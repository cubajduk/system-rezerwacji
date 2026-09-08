package pl.fabrykaterapii.reservations.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pl.fabrykaterapii.reservations.domain.TherapyService;
import pl.fabrykaterapii.reservations.repository.CatalogImportRepository;
import pl.fabrykaterapii.reservations.repository.TherapyServiceRepository;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ServiceCatalogInitializerTest {
    @Autowired ServiceCatalogInitializer initializer;
    @Autowired TherapyServiceRepository services;
    @Autowired CatalogImportRepository imports;

    @Test void importsFullCatalogAndDoesNotOverwriteEditsOrRestoreDeletedServices() throws Exception {
        services.deleteAll(); services.flush(); imports.deleteAll(); imports.flush();
        initializer.run(null);
        assertThat(services.count()).isEqualTo(51);
        var speech=services.findAll().stream().filter(s->s.getName().equals("Terapia logopedyczna 60 min")).findFirst().orElseThrow();
        assertThat(speech.getDefaultPrice()).isEqualByComparingTo("160");
        assertThat(speech.getSuggestedDurationMinutes()).isEqualTo(60);
        assertThat(services.findAll()).allSatisfy(s->{
            assertThat(s.getSuggestedDurationMinutes()).isPositive();
            assertThat(s.getDescription()).isNotBlank();
        });
        speech.update("Własna nazwa",40,new BigDecimal("199"),"","Własny opis");
        services.save(speech);
        var deleted=services.findAll().stream().filter(s->s.getName().equals("ADOS-2")).findFirst().orElseThrow();
        deleted.delete(); services.save(deleted);
        initializer.run(null);
        assertThat(services.count()).isEqualTo(51);
        assertThat(services.findById(speech.getId()).orElseThrow().getDefaultPrice()).isEqualByComparingTo("199");
        assertThat(services.findById(deleted.getId()).orElseThrow().isDeleted()).isTrue();
    }

    @Test void upgradesDemoCatalogWithoutLosingIdsOrCustomServices() throws Exception {
        services.deleteAll(); services.flush(); imports.deleteAll(); imports.flush();
        var old=services.save(new TherapyService("Terapia logopedyczna",55,new BigDecimal("180"),"Lustro logopedyczne"));
        var consultation=services.save(new TherapyService("Konsultacja psychologiczna",55,new BigDecimal("200"),""));
        var custom=services.save(new TherapyService("Moja usługa",70,new BigDecimal("333"),""));
        initializer.run(null);
        assertThat(services.findById(old.getId()).orElseThrow().isDeleted()).isTrue();
        assertThat(services.findById(consultation.getId()).orElseThrow().getDefaultPrice()).isEqualByComparingTo("250");
        assertThat(services.findById(custom.getId()).orElseThrow().getDefaultPrice()).isEqualByComparingTo("333");
        assertThat(services.findAll().stream().filter(s->!s.isDeleted()).count()).isEqualTo(52);
    }
}
