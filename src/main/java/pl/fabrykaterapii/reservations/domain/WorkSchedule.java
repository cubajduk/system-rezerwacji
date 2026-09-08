package pl.fabrykaterapii.reservations.domain;

import jakarta.persistence.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Entity @Table(name="work_schedules")
public class WorkSchedule {
 @Id @GeneratedValue(strategy=GenerationType.UUID) private String id;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) private Specialist specialist;
 @Column(nullable=false) private LocalDate startDate; private LocalDate endDate;
 @Column(nullable=false) private LocalTime startTime; @Column(nullable=false) private LocalTime endTime;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private WorkScheduleRecurrence recurrence;
 @ElementCollection(targetClass=DayOfWeek.class,fetch=FetchType.EAGER) @CollectionTable(name="work_schedule_days",joinColumns=@JoinColumn(name="work_schedule_id")) @Column(name="day_of_week") @Enumerated(EnumType.STRING) private Set<DayOfWeek> daysOfWeek=new HashSet<>();
 protected WorkSchedule(){} public WorkSchedule(Specialist specialist,LocalDate startDate,LocalDate endDate,LocalTime startTime,LocalTime endTime,WorkScheduleRecurrence recurrence,Set<DayOfWeek> days){this.specialist=specialist;this.startDate=startDate;this.endDate=endDate;this.startTime=startTime;this.endTime=endTime;this.recurrence=recurrence;this.daysOfWeek=new HashSet<>(days);}
 public String getId(){return id;} public Specialist getSpecialist(){return specialist;} public LocalDate getStartDate(){return startDate;} public LocalDate getEndDate(){return endDate;} public LocalTime getStartTime(){return startTime;} public LocalTime getEndTime(){return endTime;} public WorkScheduleRecurrence getRecurrence(){return recurrence;} public Set<DayOfWeek> getDaysOfWeek(){return Set.copyOf(daysOfWeek);}
 public void update(LocalDate startDate,LocalDate endDate,LocalTime startTime,LocalTime endTime,WorkScheduleRecurrence recurrence,Set<DayOfWeek> days){this.startDate=startDate;this.endDate=endDate;this.startTime=startTime;this.endTime=endTime;this.recurrence=recurrence;this.daysOfWeek=new HashSet<>(days);}
 public boolean covers(LocalDateTime start,LocalDateTime end){LocalDate day=start.toLocalDate();if(!end.toLocalDate().equals(day)||start.toLocalTime().isBefore(startTime)||end.toLocalTime().isAfter(endTime)||day.isBefore(startDate)||(endDate!=null&&day.isAfter(endDate)))return false;if(recurrence==WorkScheduleRecurrence.ONCE)return day.equals(startDate);if(recurrence==WorkScheduleRecurrence.MONTHLY)return day.getDayOfMonth()==startDate.getDayOfMonth();if(!daysOfWeek.contains(day.getDayOfWeek()))return false;return recurrence!=WorkScheduleRecurrence.BIWEEKLY||ChronoUnit.WEEKS.between(startDate,day)%2==0;}
}
