package pl.fabrykaterapii.reservations.repository;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import pl.fabrykaterapii.reservations.domain.AvailabilityBlock; import java.time.LocalDateTime; import java.util.*;
public interface AvailabilityBlockRepository extends JpaRepository<AvailabilityBlock,String> {
 @Query("select b from AvailabilityBlock b where (b.specialist.id=:specialistId or b.room.id=:roomId) and b.startsAt < :endsAt and b.endsAt > :startsAt") List<AvailabilityBlock> findConflicts(@Param("specialistId") String specialistId,@Param("roomId") String roomId,@Param("startsAt") LocalDateTime startsAt,@Param("endsAt") LocalDateTime endsAt);
}
