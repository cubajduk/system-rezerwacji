package pl.fabrykaterapii.reservations.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pl.fabrykaterapii.reservations.domain.ClinicSettings;
public interface ClinicSettingsRepository extends JpaRepository<ClinicSettings,Long> {}
