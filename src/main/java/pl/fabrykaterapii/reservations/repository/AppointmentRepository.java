package pl.fabrykaterapii.reservations.repository;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import pl.fabrykaterapii.reservations.domain.*; import java.time.LocalDateTime; import java.util.*;
public interface AppointmentRepository extends JpaRepository<Appointment,String> {
    @Query("select a from Appointment a where a.status not in :terminal and ((a.specialist.id=:specialistId) or (a.room.id=:roomId)) and a.startsAt < :endsAt and a.endsAt > :startsAt")
    List<Appointment> findConflicts(@Param("specialistId") String specialistId, @Param("roomId") String roomId, @Param("startsAt") LocalDateTime startsAt, @Param("endsAt") LocalDateTime endsAt, @Param("terminal") Collection<AppointmentStatus> terminal);
    @Query("select a from Appointment a where a.startsAt >= :from and a.startsAt < :to order by a.startsAt") List<Appointment> findCalendar(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
