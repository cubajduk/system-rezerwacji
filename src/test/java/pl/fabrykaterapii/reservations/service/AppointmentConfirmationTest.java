package pl.fabrykaterapii.reservations.service;

import org.junit.jupiter.api.Test;
import pl.fabrykaterapii.reservations.api.ApiDtos.AppointmentRequest;
import pl.fabrykaterapii.reservations.domain.*;
import pl.fabrykaterapii.reservations.repository.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class AppointmentConfirmationTest {
 @Test void confirmationDefaultsToTrueAndCanBeChangedWithoutAffectingOtherEdits(){
  var clients=mock(ClientRepository.class);var specialists=mock(SpecialistRepository.class);var rooms=mock(RoomRepository.class);
  var appointments=mock(AppointmentRepository.class);var specialist=mock(Specialist.class);var room=mock(Room.class);
  when(clients.findById("c")).thenReturn(Optional.of(mock(Client.class)));
  when(specialists.findByIdForUpdate("s")).thenReturn(Optional.of(specialist));when(specialist.isActive()).thenReturn(true);
  when(rooms.findByIdForUpdate("r")).thenReturn(Optional.of(room));when(room.isActive()).thenReturn(true);
  when(appointments.save(any())).thenAnswer(i->i.getArgument(0));
  var service=new AppointmentService(clients,specialists,rooms,mock(TherapyServiceRepository.class),appointments,mock(AvailabilityBlockRepository.class),mock(WorkScheduleRepository.class));
  assertThat(service.create(request(null)).getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
  var appointment=service.create(request(false));assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
  when(appointments.findById("a")).thenReturn(Optional.of(appointment));
  service.update("a",request(null));assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
  service.update("a",request(true));assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CONFIRMED);
  service.update("a",request(false));assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
  appointment.changeStatus(AppointmentStatus.COMPLETED);
  assertThatThrownBy(()->service.update("a",request(false))).isInstanceOf(IllegalArgumentException.class);
 }
 private AppointmentRequest request(Boolean confirmed){return new AppointmentRequest("c","s","r",null,LocalDateTime.of(2026,10,1,9,0),LocalDateTime.of(2026,10,1,9,55),BigDecimal.ZERO,false,"","NONE",null,null,null,null,false,confirmed);}
}
