package pl.fabrykaterapii.reservations.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pl.fabrykaterapii.reservations.domain.CatalogImport;
import pl.fabrykaterapii.reservations.domain.TherapyService;
import pl.fabrykaterapii.reservations.repository.CatalogImportRepository;
import pl.fabrykaterapii.reservations.repository.TherapyServiceRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Reviewed snapshot: no network dependency and no overwriting edits on restart. */
@Component
public class ServiceCatalogInitializer implements ApplicationRunner {
    private static final String IMPORT_ID = "fabrykaterapii-cennik-2026-09-07";
    private final TherapyServiceRepository services;
    private final CatalogImportRepository imports;
    private final ObjectMapper mapper;

    public ServiceCatalogInitializer(TherapyServiceRepository services, CatalogImportRepository imports, ObjectMapper mapper) {
        this.services = services; this.imports = imports; this.mapper = mapper;
    }

    @Override @Transactional
    public void run(ApplicationArguments args) throws Exception {
        if (imports.existsById(IMPORT_ID)) return;
        Catalog catalog;
        try (var input = new ClassPathResource("catalog/services-2026-09-07.json").getInputStream()) {
            catalog = mapper.readValue(input, Catalog.class);
        }
        var existing = services.findAll();
        // Retire untouched demo entries while retaining all appointment foreign keys.
        for (var service : existing) {
            if (!service.isDeleted() && isOriginalDemo(service)
                    && catalog.entries().stream().noneMatch(e -> e.name().equals(service.getName()))) {
                service.delete(); services.save(service);
            }
        }
        for (var entry : catalog.entries()) {
            var match = existing.stream().filter(s -> s.getName().equalsIgnoreCase(entry.name())).findFirst();
            if (match.isPresent() && (match.get().isDeleted() || !isOriginalDemo(match.get()))) continue;
            var service = match.orElseGet(() -> new TherapyService(entry.name(), entry.duration(), entry.price(), ""));
            service.update(entry.name(), entry.duration() == null ? 55 : entry.duration(), entry.price(),
                    service.getRequiredEquipment(), entry.description());
            services.save(service);
        }
        imports.save(new CatalogImport(IMPORT_ID));
    }

    private boolean isOriginalDemo(TherapyService service) {
        var prices = Map.of("Terapia logopedyczna", 180, "Integracja sensoryczna", 190,
                "Terapia ręki", 170, "Konsultacja psychologiczna", 200, "Trening Umiejętności Społecznych", 150);
        var equipment = Map.of("Terapia logopedyczna", "Lustro logopedyczne", "Integracja sensoryczna", "Wyposażenie SI",
                "Terapia ręki", "Wyposażenie do terapii ręki", "Konsultacja psychologiczna", "", "Trening Umiejętności Społecznych", "Sala grupowa");
        var price = prices.get(service.getName());
        int duration = service.getName().equals("Trening Umiejętności Społecznych") ? 60 : 55;
        return price != null && service.getDefaultPrice() != null
                && service.getDefaultPrice().compareTo(BigDecimal.valueOf(price)) == 0
                && Objects.equals(service.getSuggestedDurationMinutes(), duration)
                && Objects.equals(service.getRequiredEquipment(), equipment.get(service.getName()));
    }

    public record Catalog(String source, String retrievedAt, List<Entry> entries) {}
    public record Entry(String name, BigDecimal price, Integer duration, String description) {}
}
