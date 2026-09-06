package pl.fabrykaterapii.reservations.repository;
import org.springframework.data.jpa.repository.JpaRepository; import pl.fabrykaterapii.reservations.domain.TherapyService;
public interface TherapyServiceRepository extends JpaRepository<TherapyService,String> {}
