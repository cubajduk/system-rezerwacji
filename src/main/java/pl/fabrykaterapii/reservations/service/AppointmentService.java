package pl.fabrykaterapii.reservations.service;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import pl.fabrykaterapii.reservations.api.ApiDtos.*; import pl.fabrykaterapii.reservations.domain.*; import pl.fabrykaterapii.reservations.repository.*; import java.util.*;
@Service
public class AppointmentService {
 private static final Set<AppointmentStatus> TERMINAL=Set.of(AppointmentStatus.CANCELLED_BY_CLIENT,AppointmentStatus.CANCELLED_BY_CLINIC,AppointmentStatus.NO_SHOW);
 private final ClientRepository clients; private final SpecialistRepository specialists; private final RoomRepository rooms; private final TherapyServiceRepository services; private final AppointmentRepository appointments; private final AvailabilityBlockRepository blocks;
 public AppointmentService(ClientRepository clients,SpecialistRepository specialists,RoomRepository rooms,TherapyServiceRepository services,AppointmentRepository appointments,AvailabilityBlockRepository blocks){this.clients=clients;this.specialists=specialists;this.rooms=rooms;this.services=services;this.appointments=appointments;this.blocks=blocks;}
 @Transactional
 public Appointment create(AppointmentRequest request){
  validateRange(request.startsAt(),request.endsAt()); Client client=get(clients,request.clientId(),"Klient");
  // Blokady zasobów serializują równoczesne próby rezerwacji dla tego samego specjalisty/gabinetu.
  Specialist specialist=specialists.findByIdForUpdate(request.specialistId()).orElseThrow(()->new NoSuchElementException("Nie znaleziono specjalisty"));
  Room room=rooms.findByIdForUpdate(request.roomId()).orElseThrow(()->new NoSuchElementException("Nie znaleziono gabinetu"));
  if(!specialist.isActive()||!room.isActive()) throw new IllegalArgumentException("Wybrany specjalista lub gabinet jest nieaktywny");
  if(!appointments.findConflicts(specialist.getId(),room.getId(),request.startsAt(),request.endsAt(),TERMINAL).isEmpty()) throw new IllegalArgumentException("Termin koliduje z istniejącą wizytą specjalisty lub gabinetu");
  if(!blocks.findConflicts(specialist.getId(),room.getId(),request.startsAt(),request.endsAt()).isEmpty()) throw new IllegalArgumentException("Termin koliduje z blokadą lub niedostępnością");
  TherapyService service=request.serviceId()==null?null:get(services,request.serviceId(),"Usługa");
  java.math.BigDecimal price=request.price()!=null?request.price():(service!=null&&service.getDefaultPrice()!=null?service.getDefaultPrice():java.math.BigDecimal.ZERO);
  return appointments.save(new Appointment(client,specialist,room,service,request.startsAt(),request.endsAt(),price,request.paid(),request.note()));
 }
 @Transactional public Appointment changeStatus(String id,AppointmentStatus status){Appointment a=get(appointments,id,"Wizyta"); validateTransition(a.getStatus(),status); a.changeStatus(status); return a;}
 private void validateRange(java.time.LocalDateTime from,java.time.LocalDateTime to){if(!to.isAfter(from))throw new IllegalArgumentException("Godzina zakończenia musi być późniejsza niż rozpoczęcia");}
 private void validateTransition(AppointmentStatus from,AppointmentStatus to){if(from==AppointmentStatus.COMPLETED||TERMINAL.contains(from))throw new IllegalArgumentException("Nie można zmienić statusu wizyty zakończonej lub anulowanej"); if(from==AppointmentStatus.SCHEDULED && !(to==AppointmentStatus.CONFIRMED||to==AppointmentStatus.IN_PROGRESS||to==AppointmentStatus.CANCELLED_BY_CLIENT||to==AppointmentStatus.CANCELLED_BY_CLINIC||to==AppointmentStatus.NO_SHOW))throw new IllegalArgumentException("Niedozwolona zmiana statusu"); if(from==AppointmentStatus.CONFIRMED&&!(to==AppointmentStatus.IN_PROGRESS||to==AppointmentStatus.CANCELLED_BY_CLIENT||to==AppointmentStatus.CANCELLED_BY_CLINIC||to==AppointmentStatus.NO_SHOW))throw new IllegalArgumentException("Niedozwolona zmiana statusu"); if(from==AppointmentStatus.IN_PROGRESS&&to!=AppointmentStatus.COMPLETED)throw new IllegalArgumentException("Wizytę w toku można tylko zakończyć");}
 private <T> T get(org.springframework.data.jpa.repository.JpaRepository<T,String> repository,String id,String type){return repository.findById(id).orElseThrow(()->new NoSuchElementException("Nie znaleziono: "+type));}
}
