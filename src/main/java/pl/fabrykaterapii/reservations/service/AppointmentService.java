package pl.fabrykaterapii.reservations.service;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import pl.fabrykaterapii.reservations.api.ApiDtos.*; import pl.fabrykaterapii.reservations.domain.*; import pl.fabrykaterapii.reservations.repository.*; import java.util.*;
@Service
public class AppointmentService {
 @org.springframework.beans.factory.annotation.Autowired private ClinicSettingsService clinicSettings;
 private static final Set<AppointmentStatus> TERMINAL=Set.of(AppointmentStatus.CANCELLED_BY_CLIENT,AppointmentStatus.CANCELLED_BY_CLINIC,AppointmentStatus.NO_SHOW);
 private final ClientRepository clients; private final SpecialistRepository specialists; private final RoomRepository rooms; private final TherapyServiceRepository services; private final AppointmentRepository appointments; private final AvailabilityBlockRepository blocks; private final WorkScheduleRepository schedules;
 public AppointmentService(ClientRepository clients,SpecialistRepository specialists,RoomRepository rooms,TherapyServiceRepository services,AppointmentRepository appointments,AvailabilityBlockRepository blocks,WorkScheduleRepository schedules){this.clients=clients;this.specialists=specialists;this.rooms=rooms;this.services=services;this.appointments=appointments;this.blocks=blocks;this.schedules=schedules;}
 @Transactional
 public Appointment create(AppointmentRequest request){
  validateRange(request.startsAt(),request.endsAt()); Client client=get(clients,request.clientId(),"Klient");
  // Blokady zasobów serializują równoczesne próby rezerwacji dla tego samego specjalisty/gabinetu.
  Specialist specialist=specialists.findByIdForUpdate(request.specialistId()).orElseThrow(()->new NoSuchElementException("Nie znaleziono specjalisty"));
  Room room=rooms.findByIdForUpdate(request.roomId()).orElseThrow(()->new NoSuchElementException("Nie znaleziono gabinetu"));
  if(!specialist.isActive()||!room.isActive()) throw new IllegalArgumentException("Wybrany specjalista lub gabinet jest nieaktywny");
  TherapyService service=request.serviceId()==null?null:get(services,request.serviceId(),"Usługa"); if(service!=null&&service.isDeleted())throw new IllegalArgumentException("Ta usługa została usunięta");
  java.math.BigDecimal price=request.price()!=null?request.price():(service!=null&&service.getDefaultPrice()!=null?service.getDefaultPrice():java.math.BigDecimal.ZERO);
  List<java.time.LocalDateTime[]> seriesOccurrences=occurrences(request);
  for(java.time.LocalDateTime[] occurrence:seriesOccurrences) ensureFree(specialist,room,occurrence[0],occurrence[1]);
  String recurrenceGroupId="NONE".equals(request.recurrence())?null:java.util.UUID.randomUUID().toString();
  Appointment first=null;
  for(java.time.LocalDateTime[] occurrence:seriesOccurrences){
   Appointment saved=appointments.save(new Appointment(client,specialist,room,service,occurrence[0],occurrence[1],price,request.paid(),request.note(),recurrenceGroupId,request.recurrence()));
   saved.changeStatus(Boolean.FALSE.equals(request.confirmed())?AppointmentStatus.SCHEDULED:AppointmentStatus.CONFIRMED);
   if(first==null) first=saved;
  }
  return first;
 }
 private List<java.time.LocalDateTime[]> occurrences(AppointmentRequest request){
  List<java.time.LocalDateTime[]> result=new ArrayList<>();
  String recurrence=request.recurrence()==null?"NONE":request.recurrence();
  java.time.Duration duration=java.time.Duration.between(request.startsAt(),request.endsAt());
  int maximumMonths=clinicSettings==null?12:clinicSettings.read().recurrenceMonths();
  java.time.LocalDate maximum=request.startsAt().toLocalDate().plusMonths(maximumMonths);
  java.time.LocalDate last=request.recurrenceEndDate()==null?maximum:request.recurrenceEndDate();
  if(!"NONE".equals(recurrence)&&last.isAfter(maximum))throw new IllegalArgumentException("Cykl wizyt może trwać maksymalnie " + maximumMonths + " miesięcy");
  if(last.isBefore(request.startsAt().toLocalDate())) throw new IllegalArgumentException("Data zakończenia powtarzania nie może być wcześniejsza od daty wizyty");
  java.time.LocalDate date=request.startsAt().toLocalDate();
  int interval=Math.max(1,request.recurrenceInterval()==null?1:request.recurrenceInterval());
  while(!date.isAfter(last)){
   boolean include=switch(recurrence){case "NONE"->date.equals(request.startsAt().toLocalDate());case "WEEKDAYS"->date.getDayOfWeek().getValue()<=5;case "DAILY"->true;case "WEEKLY"->date.getDayOfWeek()==request.startsAt().getDayOfWeek();case "BIWEEKLY"->date.getDayOfWeek()==request.startsAt().getDayOfWeek()&&java.time.temporal.ChronoUnit.WEEKS.between(request.startsAt().toLocalDate(),date)%2==0;case "MONTHLY"->date.getDayOfMonth()==request.startsAt().getDayOfMonth();case "YEARLY"->date.getMonthValue()==request.startsAt().getMonthValue()&&date.getDayOfMonth()==request.startsAt().getDayOfMonth();case "CUSTOM"->customOccurs(request,date,interval);default->false;};
   if(include){java.time.LocalDateTime start=java.time.LocalDateTime.of(date,request.startsAt().toLocalTime());result.add(new java.time.LocalDateTime[]{start,start.plus(duration)});}
   if("NONE".equals(recurrence)) break;
   date=date.plusDays(1);
  }
  return result;
 }
 private boolean customOccurs(AppointmentRequest request,java.time.LocalDate date,int interval){
  java.time.LocalDate start=request.startsAt().toLocalDate();
  String unit=request.recurrenceUnit()==null?"WEEKS":request.recurrenceUnit();
  if(date.isBefore(start)) return false;
  return switch(unit){case "DAYS"->java.time.temporal.ChronoUnit.DAYS.between(start,date)%interval==0;case "WEEKS"->(request.recurrenceDays()==null||request.recurrenceDays().isEmpty()?date.getDayOfWeek()==start.getDayOfWeek():request.recurrenceDays().contains(date.getDayOfWeek()))&&java.time.temporal.ChronoUnit.WEEKS.between(start,date)%interval==0;case "MONTHS"->date.getDayOfMonth()==start.getDayOfMonth()&&java.time.temporal.ChronoUnit.MONTHS.between(start.withDayOfMonth(1),date.withDayOfMonth(1))%interval==0;case "YEARS"->date.getMonthValue()==start.getMonthValue()&&date.getDayOfMonth()==start.getDayOfMonth()&&java.time.temporal.ChronoUnit.YEARS.between(start,date)%interval==0;default->false;};
 }
 private void ensureFree(Specialist specialist,Room room,java.time.LocalDateTime from,java.time.LocalDateTime to){var conflicts=appointments.findConflicts(specialist.getId(),room.getId(),from,to,TERMINAL);if(!conflicts.isEmpty()){var conflict=conflicts.getFirst();throw new IllegalArgumentException("Konflikt: " + conflict.getStartsAt()+" — wizyta " + conflict.getClient().getFirstName()+" "+conflict.getClient().getLastName()+" ("+conflict.getSpecialist().getFirstName()+" "+conflict.getSpecialist().getLastName()+")");}if(!blocks.findConflicts(specialist.getId(),room.getId(),from,to).isEmpty())throw new IllegalArgumentException("Termin koliduje z blokadą lub niedostępnością");}
 @Transactional public Appointment changeStatus(String id,AppointmentStatus status){Appointment a=get(appointments,id,"Wizyta"); validateTransition(a.getStatus(),status); a.changeStatus(status); return a;}
 @Transactional public void delete(String id){delete(id,false);}
 @Transactional public void delete(String id,boolean deleteSeries){var appointment=get(appointments,id,"Wizyta");if(deleteSeries&&appointment.getRecurrenceGroupId()!=null)appointments.deleteAll(appointments.findByRecurrenceGroupIdAndStartsAtGreaterThanEqualOrderByStartsAt(appointment.getRecurrenceGroupId(),appointment.getStartsAt()));else appointments.delete(appointment);}
 @Transactional public Appointment updatePayment(String id,java.math.BigDecimal price,boolean paid){var appointment=get(appointments,id,"Wizyta");appointment.updatePayment(price,paid);return appointment;}
 @Transactional public Appointment update(String id,AppointmentRequest request){
  validateRange(request.startsAt(),request.endsAt()); Appointment appointment=get(appointments,id,"Wizyta"); Client client=get(clients,request.clientId(),"Klient");
  Specialist specialist=specialists.findByIdForUpdate(request.specialistId()).orElseThrow(()->new NoSuchElementException("Nie znaleziono specjalisty"));
  Room room=rooms.findByIdForUpdate(request.roomId()).orElseThrow(()->new NoSuchElementException("Nie znaleziono gabinetu"));
  if(!specialist.isActive()||!room.isActive()) throw new IllegalArgumentException("Wybrany specjalista lub gabinet jest nieaktywny");
  TherapyService service=request.serviceId()==null?null:get(services,request.serviceId(),"Usługa"); if(service!=null&&service.isDeleted()&&(appointment.getService()==null||!service.getId().equals(appointment.getService().getId())))throw new IllegalArgumentException("Ta usługa została usunięta"); java.math.BigDecimal price=request.price()!=null?request.price():(service!=null&&service.getDefaultPrice()!=null?service.getDefaultPrice():java.math.BigDecimal.ZERO);
  applyConfirmation(appointment,request.confirmed());
  if(request.updateSeries()&&appointment.getRecurrenceGroupId()!=null){
   var series=appointments.findByRecurrenceGroupIdAndStartsAtGreaterThanEqualOrderByStartsAt(appointment.getRecurrenceGroupId(),appointment.getStartsAt());
   var startShift=java.time.Duration.between(appointment.getStartsAt(),request.startsAt()); var endShift=java.time.Duration.between(appointment.getEndsAt(),request.endsAt());
   for(Appointment item:series) ensureFreeExcluding(item.getId(),specialist,room,item.getStartsAt().plus(startShift),item.getEndsAt().plus(endShift));
   for(Appointment item:series) item.update(client,specialist,room,service,item.getStartsAt().plus(startShift),item.getEndsAt().plus(endShift),price,request.paid(),request.note());
   return appointment;
  }
  ensureFreeExcluding(id,specialist,room,request.startsAt(),request.endsAt());
  appointment.update(client,specialist,room,service,request.startsAt(),request.endsAt(),price,request.paid(),request.note()); return appointment;
 }
 private void applyConfirmation(Appointment appointment,Boolean confirmed){
  if(confirmed==null)return;
  if(appointment.getStatus()!=AppointmentStatus.SCHEDULED&&appointment.getStatus()!=AppointmentStatus.CONFIRMED)throw new IllegalArgumentException("Potwierdzenie można zmieniać tylko dla zaplanowanej wizyty");
  appointment.changeStatus(confirmed?AppointmentStatus.CONFIRMED:AppointmentStatus.SCHEDULED);
 }
 private void ensureFreeExcluding(String id,Specialist specialist,Room room,java.time.LocalDateTime from,java.time.LocalDateTime to){var conflicts=appointments.findConflictsExcluding(id,specialist.getId(),room.getId(),from,to,TERMINAL);if(!conflicts.isEmpty()){var conflict=conflicts.getFirst();throw new IllegalArgumentException("Konflikt: "+conflict.getStartsAt()+" — wizyta "+conflict.getClient().getFirstName()+" "+conflict.getClient().getLastName()+" ("+conflict.getSpecialist().getFirstName()+" "+conflict.getSpecialist().getLastName()+")");}if(!blocks.findConflicts(specialist.getId(),room.getId(),from,to).isEmpty())throw new IllegalArgumentException("Termin koliduje z blokadą lub niedostępnością");}
 private void validateRange(java.time.LocalDateTime from,java.time.LocalDateTime to){if(!to.isAfter(from))throw new IllegalArgumentException("Godzina zakończenia musi być późniejsza niż rozpoczęcia");}
 private void ensureWithinWorkingHours(Specialist specialist,java.time.LocalDateTime from,java.time.LocalDateTime to){var work=schedules.findBySpecialistId(specialist.getId());if(!work.isEmpty()&&work.stream().noneMatch(s->s.covers(from,to)))throw new IllegalArgumentException("Termin nie mieści się w zdefiniowanych godzinach pracy specjalisty");}
 private void validateTransition(AppointmentStatus from,AppointmentStatus to){if(from==AppointmentStatus.COMPLETED||TERMINAL.contains(from))throw new IllegalArgumentException("Nie można zmienić statusu wizyty zakończonej lub anulowanej"); if(from==AppointmentStatus.SCHEDULED && !(to==AppointmentStatus.CONFIRMED||to==AppointmentStatus.IN_PROGRESS||to==AppointmentStatus.CANCELLED_BY_CLIENT||to==AppointmentStatus.CANCELLED_BY_CLINIC||to==AppointmentStatus.NO_SHOW))throw new IllegalArgumentException("Niedozwolona zmiana statusu"); if(from==AppointmentStatus.CONFIRMED&&!(to==AppointmentStatus.IN_PROGRESS||to==AppointmentStatus.CANCELLED_BY_CLIENT||to==AppointmentStatus.CANCELLED_BY_CLINIC||to==AppointmentStatus.NO_SHOW))throw new IllegalArgumentException("Niedozwolona zmiana statusu"); if(from==AppointmentStatus.IN_PROGRESS&&to!=AppointmentStatus.COMPLETED)throw new IllegalArgumentException("Wizytę w toku można tylko zakończyć");}
 private <T> T get(org.springframework.data.jpa.repository.JpaRepository<T,String> repository,String id,String type){return repository.findById(id).orElseThrow(()->new NoSuchElementException("Nie znaleziono: "+type));}
}
