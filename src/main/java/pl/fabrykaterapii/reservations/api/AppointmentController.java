package pl.fabrykaterapii.reservations.api;
import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.web.bind.annotation.*; import pl.fabrykaterapii.reservations.api.ApiDtos.*; import pl.fabrykaterapii.reservations.repository.AppointmentRepository; import pl.fabrykaterapii.reservations.service.AppointmentService; import java.time.*; import java.util.*;
@RestController @RequestMapping("/api/appointments")
public class AppointmentController {
 private final AppointmentRepository repository; private final AppointmentService service; public AppointmentController(AppointmentRepository repository,AppointmentService service){this.repository=repository;this.service=service;}
 @GetMapping @PreAuthorize("hasAnyRole('OWNER','MANAGER','RECEPTION','SPECIALIST')") List<AppointmentResponse> calendar(@RequestParam LocalDate from,@RequestParam LocalDate to){if(to.isBefore(from))throw new IllegalArgumentException("Nieprawidłowy zakres dat");return repository.findCalendar(from.atStartOfDay(),to.plusDays(1).atStartOfDay()).stream().map(AppointmentResponse::of).toList();}
 @PostMapping @PreAuthorize("hasAnyRole('OWNER','MANAGER','RECEPTION','SPECIALIST')") ResponseEntity<AppointmentResponse> create(@jakarta.validation.Valid @RequestBody AppointmentRequest request){return ResponseEntity.status(HttpStatus.CREATED).body(AppointmentResponse.of(service.create(request)));}
 @PatchMapping("/{id}/status") @PreAuthorize("hasAnyRole('OWNER','MANAGER','RECEPTION','SPECIALIST')") AppointmentResponse status(@PathVariable String id,@jakarta.validation.Valid @RequestBody StatusRequest r){return AppointmentResponse.of(service.changeStatus(id,r.status()));}
}
