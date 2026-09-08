package pl.fabrykaterapii.reservations.service;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.fabrykaterapii.reservations.api.ApiDtos.AppointmentRequest;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class RecurrenceSettingsTest {
 @Test void configuredLimitAppliesToDefaultAndExplicitEndDates(){
  var service=new AppointmentService(null,null,null,null,null,null,null);
  var settings=mock(ClinicSettingsService.class);
  when(settings.read()).thenReturn(new ClinicSettingsService.Settings("Test","","","","","","week",true,true,true,2,8,18,"#123456"));
  ReflectionTestUtils.setField(service,"clinicSettings",settings);
  List<LocalDateTime[]> dates=ReflectionTestUtils.invokeMethod(service,"occurrences",request(null));
  assertThat(dates).hasSize(9);
  assertThatThrownBy(()->ReflectionTestUtils.invokeMethod(service,"occurrences",request(LocalDate.of(2026,4,1)))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("2 miesięcy");
 }
 private AppointmentRequest request(LocalDate end){return new AppointmentRequest("c","s","r",null,LocalDateTime.of(2026,1,1,9,0),LocalDateTime.of(2026,1,1,10,0),null,false,"","WEEKLY",end,null,null,null,false,true);}
}
