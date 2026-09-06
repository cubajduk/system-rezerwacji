package pl.fabrykaterapii.reservations.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.OffsetDateTime;
import java.util.Map;

/** Publiczny endpoint kontrolny używany po wdrożeniu do potwierdzenia działania API. */
@RestController
@RequestMapping("/api")
public class HelloController {
    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of(
                "message", "Hello World - Fabryka Terapii API działa",
                "timestamp", OffsetDateTime.now().toString()
        );
    }
}
