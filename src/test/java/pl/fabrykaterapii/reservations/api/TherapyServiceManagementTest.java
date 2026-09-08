package pl.fabrykaterapii.reservations.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import pl.fabrykaterapii.reservations.repository.TherapyServiceRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles="OWNER")
class TherapyServiceManagementTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired TherapyServiceRepository services;

    @Test void createEditAndWithdrawServiceRetainsHistoricalReference() throws Exception {
        String created=mvc.perform(post("/api/services").contentType("application/json").content("""
            {"name":"Usługa testowa","description":"Krótki opis","defaultPrice":123.45,"suggestedDurationMinutes":45}
            """)).andExpect(status().isCreated()).andExpect(jsonPath("$.description").value("Krótki opis"))
            .andReturn().getResponse().getContentAsString();
        String id=json.readTree(created).get("id").asText();
        mvc.perform(patch("/api/services/"+id).contentType("application/json").content("""
            {"name":"Zmieniona usługa","description":"Nowy opis","defaultPrice":0,"suggestedDurationMinutes":60}
            """)).andExpect(status().isOk()).andExpect(jsonPath("$.defaultPrice").value(0))
            .andExpect(jsonPath("$.name").value("Zmieniona usługa"));
        mvc.perform(delete("/api/services/"+id)).andExpect(status().isNoContent());
        assertThat(services.findById(id)).hasValueSatisfying(s->{
            assertThat(s.isDeleted()).isTrue();
            assertThat(s.getDescription()).isEqualTo("Nowy opis");
        });
        mvc.perform(patch("/api/services/"+id).contentType("application/json").content("""
            {"name":"Nie przywracaj","defaultPrice":10}
            """)).andExpect(status().isNotFound());
    }

    @Test void rejectsInvalidPricesAndBlankNames() throws Exception {
        for(String body:new String[]{"{\"name\":\"Test\",\"defaultPrice\":-1}","{\"name\":\"Test\",\"defaultPrice\":1.234}","{\"name\":\"Test\"}","{\"name\":\" \",\"defaultPrice\":10}"})
            mvc.perform(post("/api/services").contentType("application/json").content(body)).andExpect(status().isBadRequest());
    }

    @Test @WithMockUser(roles="RECEPTION") void receptionCannotModifyCatalog() throws Exception {
        mvc.perform(post("/api/services").contentType("application/json").content("{\"name\":\"Test\",\"defaultPrice\":10}")).andExpect(status().isForbidden());
    }
}
