package pl.fabrykaterapii.reservations.repository;
import org.springframework.data.jpa.repository.*; import org.springframework.data.repository.query.Param; import pl.fabrykaterapii.reservations.domain.*; import java.time.LocalDateTime; import java.util.*;
public interface AppointmentRepository extends JpaRepository<Appointment,String> {
    @Query("select min(a.startsAt) from Appointment a where a.status not in :terminal and a.startsAt >= :from and (:after is null or a.startsAt > :after)")
    LocalDateTime nextSummaryTime(LocalDateTime from,LocalDateTime after,Collection<AppointmentStatus> terminal);
    @Query("select max(a.startsAt) from Appointment a where a.status not in :terminal and a.startsAt >= :from and a.startsAt < :before")
    LocalDateTime previousSummaryTime(LocalDateTime from,LocalDateTime before,Collection<AppointmentStatus> terminal);
    List<Appointment> findByStartsAtOrderById(LocalDateTime startsAt);
    long countByStatus(AppointmentStatus status);
    @Query("select a from Appointment a where a.status not in :terminal and ((a.specialist.id=:specialistId) or (a.room.id=:roomId)) and a.startsAt < :endsAt and a.endsAt > :startsAt")
    List<Appointment> findConflicts(@Param("specialistId") String specialistId, @Param("roomId") String roomId, @Param("startsAt") LocalDateTime startsAt, @Param("endsAt") LocalDateTime endsAt, @Param("terminal") Collection<AppointmentStatus> terminal);
    @Query("select a from Appointment a where a.id <> :appointmentId and a.status not in :terminal and ((a.specialist.id=:specialistId) or (a.room.id=:roomId)) and a.startsAt < :endsAt and a.endsAt > :startsAt")
    List<Appointment> findConflictsExcluding(@Param("appointmentId") String appointmentId, @Param("specialistId") String specialistId, @Param("roomId") String roomId, @Param("startsAt") LocalDateTime startsAt, @Param("endsAt") LocalDateTime endsAt, @Param("terminal") Collection<AppointmentStatus> terminal);
    @Query("select a from Appointment a where a.startsAt >= :from and a.startsAt < :to order by a.startsAt") List<Appointment> findCalendar(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    @Query("select a from Appointment a where a.specialist.id=:specialistId and a.status not in :terminal") List<Appointment> findActiveBySpecialistId(@Param("specialistId") String specialistId,@Param("terminal") Collection<AppointmentStatus> terminal);
    List<Appointment> findByRecurrenceGroupIdAndStartsAtGreaterThanEqualOrderByStartsAt(String recurrenceGroupId, LocalDateTime startsAt);
    boolean existsByClientId(String clientId); boolean existsBySpecialistId(String specialistId); boolean existsByRoomId(String roomId);
}
