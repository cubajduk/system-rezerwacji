package pl.fabrykaterapii.reservations.repository;
import org.springframework.data.jpa.repository.JpaRepository; import pl.fabrykaterapii.reservations.domain.WorkSchedule; import java.util.*;
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule,String>{ List<WorkSchedule> findBySpecialistId(String specialistId); }
