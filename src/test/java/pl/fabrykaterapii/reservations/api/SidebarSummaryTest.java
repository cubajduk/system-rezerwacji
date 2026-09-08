package pl.fabrykaterapii.reservations.api;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.fabrykaterapii.reservations.repository.*;
import pl.fabrykaterapii.reservations.domain.*;
import java.time.*;
import java.math.BigDecimal;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc @Transactional
class SidebarSummaryTest {
 @Autowired MockMvc mvc;
 @Autowired AppointmentRepository appointments;
 @Autowired ClientRepository clients;
 @Autowired SpecialistRepository specialists;
 @Autowired RoomRepository rooms;
 @Test void summaryCountsCurrentWeekAndNavigatesAllVisitsAtTheSameTime() throws Exception {
  appointments.deleteAll();var now=LocalDateTime.now(ZoneId.of("Europe/Warsaw"));var first=now.plusDays(10).withHour(9).withMinute(0).withSecond(0).withNano(0);var next=first.plusDays(1);
  add(now.toLocalDate().atTime(0,0),AppointmentStatus.COMPLETED);
  add(first,AppointmentStatus.CONFIRMED);add(first,AppointmentStatus.SCHEDULED);add(next,AppointmentStatus.CONFIRMED);add(first.minusDays(1),AppointmentStatus.CANCELLED_BY_CLIENT);
  mvc.perform(get("/api/appointments/summary").with(user("owner").roles("OWNER"))).andExpect(status().isOk()).andExpect(jsonPath("$.todayCount").value(1)).andExpect(jsonPath("$.weekCount").value(1)).andExpect(jsonPath("$.visits.length()").value(2)).andExpect(jsonPath("$.hasPrevious").value(false)).andExpect(jsonPath("$.hasNext").value(true));
  mvc.perform(get("/api/appointments/summary").param("cursor",first.toString()).param("direction","next").with(user("owner").roles("OWNER"))).andExpect(status().isOk()).andExpect(jsonPath("$.visits.length()").value(1)).andExpect(jsonPath("$.hasPrevious").value(true)).andExpect(jsonPath("$.hasNext").value(false));
  mvc.perform(get("/api/appointments/summary").param("cursor",next.toString()).param("direction","previous").with(user("owner").roles("OWNER"))).andExpect(jsonPath("$.visits.length()").value(2));
 }
 @Test void confidentialEndpointsRequireAuthenticationOnEveryRequest() throws Exception {
  mvc.perform(get("/api/appointments/summary").with(httpBasic("owner@fabrykaterapii.pl","change-me"))).andExpect(status().isOk());
  for(String path:new String[]{"/api/appointments/summary","/api/clients","/api/specialists","/api/rooms","/api/settings","/h2-console/"})mvc.perform(get(path)).andExpect(status().isUnauthorized());
 }
 private void add(LocalDateTime start,AppointmentStatus status){var a=new Appointment(clients.findAll().getFirst(),specialists.findAll().getFirst(),rooms.findAll().getFirst(),null,start,start.plusMinutes(55),BigDecimal.TEN,false,"");a.changeStatus(status);appointments.saveAndFlush(a);}
}
