package pl.fabrykaterapii.reservations.repository;
import jakarta.persistence.LockModeType; import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import pl.fabrykaterapii.reservations.domain.Room; import java.util.*;
public interface RoomRepository extends JpaRepository<Room,String> { @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select r from Room r where r.id=:id") Optional<Room> findByIdForUpdate(@Param("id") String id); }
