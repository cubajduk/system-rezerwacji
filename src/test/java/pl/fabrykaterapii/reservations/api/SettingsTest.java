package pl.fabrykaterapii.reservations.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import pl.fabrykaterapii.reservations.service.ClinicSettingsService;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class SettingsTest {
 @Autowired MockMvc mvc;
 @Autowired ObjectMapper mapper;
 @Test void savesSharedPreferencesAndEnforcesRolesAndValidation() throws Exception {
  String body=mapper.writeValueAsString(ClinicSettingsService.defaults());
  mvc.perform(get("/api/settings")).andExpect(status().isUnauthorized());
  mvc.perform(put("/api/settings").with(user("reception").roles("RECEPTION")).contentType("application/json").content(body)).andExpect(status().isForbidden());
  mvc.perform(put("/api/settings").with(user("manager").roles("MANAGER")).contentType("application/json").content(body.replace("\"recurrenceMonths\":12","\"recurrenceMonths\":6"))).andExpect(status().isOk());
  mvc.perform(get("/api/settings").with(user("owner").roles("OWNER"))).andExpect(jsonPath("$.settings.recurrenceMonths").value(6)).andExpect(jsonPath("$.roles[0]").value("ROLE_OWNER"));
  mvc.perform(put("/api/settings").with(user("owner").roles("OWNER")).contentType("application/json").content(body.replace("\"recurrenceMonths\":12","\"recurrenceMonths\":0"))).andExpect(status().isBadRequest());
  mvc.perform(put("/api/settings").with(user("owner").roles("OWNER")).contentType("application/json").content(body.replace("\"closingHour\":"+ClinicSettingsService.defaults().closingHour(),"\"closingHour\":6"))).andExpect(status().isUnprocessableEntity());
  mvc.perform(put("/api/settings").with(user("owner").roles("OWNER")).contentType("application/json").content(body)).andExpect(status().isOk());
 }
}
