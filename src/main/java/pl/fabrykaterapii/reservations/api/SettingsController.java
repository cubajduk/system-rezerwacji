package pl.fabrykaterapii.reservations.api;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.fabrykaterapii.reservations.service.ClinicSettingsService;

@RestController @RequestMapping("/api/settings")
public class SettingsController {
 private final ClinicSettingsService settings;
 public SettingsController(ClinicSettingsService settings){this.settings=settings;}
 @GetMapping public Object read(Authentication auth){return Map.of("settings",settings.read(),"roles",auth.getAuthorities().stream().map(a->a.getAuthority()).toList());}
 @PutMapping @PreAuthorize("hasAnyRole('OWNER','MANAGER')")
 public Object save(@jakarta.validation.Valid @RequestBody ClinicSettingsService.Settings value){return settings.save(value);}
}
