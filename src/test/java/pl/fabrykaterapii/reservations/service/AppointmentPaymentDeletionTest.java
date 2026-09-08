package pl.fabrykaterapii.reservations.service;
import org.junit.jupiter.api.Test;
import pl.fabrykaterapii.reservations.domain.*;
import pl.fabrykaterapii.reservations.repository.*;
import java.util.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;
class AppointmentPaymentDeletionTest {
 @Test void paymentChangesOnlyFinancialFieldsEvenForInactiveResources(){
  var repo=mock(AppointmentRepository.class);var appointment=new Appointment(mock(Client.class),mock(Specialist.class),mock(Room.class),null,LocalDateTime.of(2026,9,8,9,0),LocalDateTime.of(2026,9,8,10,0),BigDecimal.TEN,false,"Notatka");
  when(repo.findById("a")).thenReturn(Optional.of(appointment));var start=appointment.getStartsAt();service(repo).updatePayment("a",new BigDecimal("248.50"),true);
  assertThat(appointment.getPrice()).isEqualByComparingTo("248.50");assertThat(appointment.isPaid()).isTrue();assertThat(appointment.getStartsAt()).isEqualTo(start);assertThat(appointment.getNote()).isEqualTo("Notatka");assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
 }
 @Test void seriesDeleteUsesSelectedDateAndSingleDeleteDoesNotQuerySeries(){
  var repo=mock(AppointmentRepository.class);var a=mock(Appointment.class);var start=LocalDateTime.of(2026,9,8,9,0);when(repo.findById("a")).thenReturn(Optional.of(a));when(a.getRecurrenceGroupId()).thenReturn("cycle");when(a.getStartsAt()).thenReturn(start);var future=List.of(a,mock(Appointment.class));when(repo.findByRecurrenceGroupIdAndStartsAtGreaterThanEqualOrderByStartsAt("cycle",start)).thenReturn(future);
  service(repo).delete("a",true);verify(repo).deleteAll(future);verify(repo,never()).delete(a);clearInvocations(repo);service(repo).delete("a",false);verify(repo).delete(a);verify(repo,never()).findByRecurrenceGroupIdAndStartsAtGreaterThanEqualOrderByStartsAt(any(),any());
 }
 private AppointmentService service(AppointmentRepository repo){return new AppointmentService(mock(ClientRepository.class),mock(SpecialistRepository.class),mock(RoomRepository.class),mock(TherapyServiceRepository.class),repo,mock(AvailabilityBlockRepository.class),mock(WorkScheduleRepository.class));}
}
