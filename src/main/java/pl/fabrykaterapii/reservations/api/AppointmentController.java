package pl.fabrykaterapii.reservations.api;
import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import pl.fabrykaterapii.reservations.api.ApiDtos.*; import pl.fabrykaterapii.reservations.repository.AppointmentRepository; import pl.fabrykaterapii.reservations.service.AppointmentService; import java.time.*; import java.util.*;
@RestController @RequestMapping("/api/appointments")
public class AppointmentController {
 private final AppointmentRepository repository; private final AppointmentService service; public AppointmentController(AppointmentRepository repository,AppointmentService service){this.repository=repository;this.service=service;}
 @GetMapping @PreAuthorize("hasAnyRole('OWNER','MANAGER','RECEPTION','SPECIALIST')") List<AppointmentResponse> calendar(@RequestParam LocalDate from,@RequestParam LocalDate to){if(to.isBefore(from))throw new IllegalArgumentException("Nieprawidłowy zakres dat");return repository.findCalendar(from.atStartOfDay(),to.plusDays(1).atStartOfDay()).stream().map(AppointmentResponse::of).toList();}
 public record SummaryVisit(String id,String startsAt,String client,String specialist) {}
 public record SummaryResponse(long todayCount,long weekCount,List<SummaryVisit> visits,LocalDateTime cursor,boolean hasPrevious,boolean hasNext) {}
 @org.springframework.transaction.annotation.Transactional(readOnly=true) @GetMapping("/summary") @PreAuthorize("hasAnyRole('OWNER','MANAGER','RECEPTION','SPECIALIST')") SummaryResponse summary(@RequestParam(required=false) LocalDateTime cursor,@RequestParam(defaultValue="current") String direction){
  var now=LocalDateTime.now(java.time.ZoneId.of("Europe/Warsaw"));var today=now.toLocalDate();var week=today.with(java.time.DayOfWeek.MONDAY);
  var terminal=List.of(pl.fabrykaterapii.reservations.domain.AppointmentStatus.CANCELLED_BY_CLIENT,pl.fabrykaterapii.reservations.domain.AppointmentStatus.CANCELLED_BY_CLINIC,pl.fabrykaterapii.reservations.domain.AppointmentStatus.NO_SHOW);
  var visits=repository.findCalendar(week.atStartOfDay(),week.plusDays(7).atStartOfDay()).stream().filter(a->!terminal.contains(a.getStatus())).toList();
  LocalDateTime selected;
  if(cursor==null||cursor.isBefore(now))selected=repository.nextSummaryTime(now,null,terminal);
  else if(direction.equals("next"))selected=repository.nextSummaryTime(now,cursor,terminal);
  else if(direction.equals("previous"))selected=repository.previousSummaryTime(now,cursor,terminal);
  else selected=repository.nextSummaryTime(cursor,null,terminal);
  if(selected==null&&cursor!=null)selected=repository.nextSummaryTime(now,null,terminal);
  var group=selected==null?List.<SummaryVisit>of():repository.findByStartsAtOrderById(selected).stream().filter(a->!terminal.contains(a.getStatus())).map(a->new SummaryVisit(a.getId(),a.getStartsAt().toString(),a.getClient().getFirstName()+" "+a.getClient().getLastName(),a.getSpecialist().getFirstName()+" "+a.getSpecialist().getLastName())).toList();
  return new SummaryResponse(visits.stream().filter(a->a.getStartsAt().toLocalDate().equals(today)).count(),visits.size(),group,selected,selected!=null&&repository.previousSummaryTime(now,selected,terminal)!=null,selected!=null&&repository.nextSummaryTime(now,selected,terminal)!=null);
 }
 @GetMapping("/pending-count") @PreAuthorize("hasAnyRole('OWNER','MANAGER','RECEPTION','SPECIALIST')") long pendingCount(){return repository.countByStatus(pl.fabrykaterapii.reservations.domain.AppointmentStatus.SCHEDULED);}
 @PostMapping @PreAuthorize("hasAnyRole('OWNER','MANAGER','RECEPTION','SPECIALIST')") ResponseEntity<AppointmentResponse> create(@jakarta.validation.Valid @RequestBody AppointmentRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.of(service.create(request)));}
 @PatchMapping("/{id}") @PreAuthorize("hasAnyRole('OWNER','MANAGER','RECEPTION','SPECIALIST')") AppointmentResponse update(@PathVariable String id,@jakarta.validation.Valid @RequestBody AppointmentRequest request){return AppointmentResponse.of(service.update(id,request));}
 @PatchMapping("/{id}/status") @PreAuthorize("hasAnyRole('OWNER','MANAGER','RECEPTION','SPECIALIST')") AppointmentResponse status(@PathVariable String id,@jakarta.validation.Valid @RequestBody StatusRequest r){return AppointmentResponse.of(service.changeStatus(id,r.status()));}
 public record PaymentRequest(@jakarta.validation.constraints.NotNull @jakarta.validation.constraints.DecimalMin("0.00") @jakarta.validation.constraints.Digits(integer=10,fraction=2) java.math.BigDecimal price,boolean paid) {}
 @PatchMapping("/{id}/payment") @PreAuthorize("hasAnyRole('OWNER','MANAGER','RECEPTION','SPECIALIST')") AppointmentResponse payment(@PathVariable String id,@jakarta.validation.Valid @RequestBody PaymentRequest request){return AppointmentResponse.of(service.updatePayment(id,request.price(),request.paid()));}
 @DeleteMapping("/{id}") @PreAuthorize("hasAnyRole('OWNER','MANAGER','RECEPTION')") ResponseEntity<Void> delete(@PathVariable String id,@RequestParam(defaultValue="false") boolean deleteSeries){service.delete(id,deleteSeries);return ResponseEntity.noContent().build();}
}
