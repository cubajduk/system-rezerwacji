package pl.fabrykaterapii.reservations.domain;

import jakarta.persistence.*; import java.math.BigDecimal; import java.time.LocalDateTime;
@Entity @Table(name="appointments", indexes={@Index(name="idx_appointment_specialist_time",columnList="specialist_id,starts_at,ends_at"),@Index(name="idx_appointment_room_time",columnList="room_id,starts_at,ends_at")})
public class Appointment {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private String id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) private Client client;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) private Specialist specialist;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) private Room room;
    @ManyToOne(fetch=FetchType.LAZY) private TherapyService service;
    @Column(nullable=false) private LocalDateTime startsAt; @Column(nullable=false) private LocalDateTime endsAt;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private AppointmentStatus status = AppointmentStatus.SCHEDULED;
    @Column(nullable=false) private BigDecimal price; @Column(nullable=false) private boolean paid; @Column(length=2000) private String note; @Column(name="recurrence_group_id") private String recurrenceGroupId; @Column(name="recurrence_type") private String recurrenceType;
    @Version private long version;
    protected Appointment(){} public Appointment(Client client, Specialist specialist, Room room, TherapyService service, LocalDateTime startsAt, LocalDateTime endsAt, BigDecimal price, boolean paid, String note){this(client,specialist,room,service,startsAt,endsAt,price,paid,note,null,null);} public Appointment(Client client, Specialist specialist, Room room, TherapyService service, LocalDateTime startsAt, LocalDateTime endsAt, BigDecimal price, boolean paid, String note,String recurrenceGroupId){this(client,specialist,room,service,startsAt,endsAt,price,paid,note,recurrenceGroupId,null);} public Appointment(Client client, Specialist specialist, Room room, TherapyService service, LocalDateTime startsAt, LocalDateTime endsAt, BigDecimal price, boolean paid, String note,String recurrenceGroupId,String recurrenceType){this.client=client;this.specialist=specialist;this.room=room;this.service=service;this.startsAt=startsAt;this.endsAt=endsAt;this.price=price;this.paid=paid;this.note=note;this.recurrenceGroupId=recurrenceGroupId;this.recurrenceType=recurrenceType;}
    public String getId(){return id;} public Client getClient(){return client;} public Specialist getSpecialist(){return specialist;} public Room getRoom(){return room;} public TherapyService getService(){return service;} public LocalDateTime getStartsAt(){return startsAt;} public LocalDateTime getEndsAt(){return endsAt;} public AppointmentStatus getStatus(){return status;} public BigDecimal getPrice(){return price;} public boolean isPaid(){return paid;} public String getNote(){return note;} public String getRecurrenceGroupId(){return recurrenceGroupId;} public String getRecurrenceType(){return recurrenceType;}
    public void updatePayment(BigDecimal price,boolean paid){this.price=price;this.paid=paid;}
    public void changeStatus(AppointmentStatus status){this.status=status;}
    public void update(Client client, Specialist specialist, Room room, TherapyService service, LocalDateTime startsAt, LocalDateTime endsAt, BigDecimal price, boolean paid, String note){this.client=client;this.specialist=specialist;this.room=room;this.service=service;this.startsAt=startsAt;this.endsAt=endsAt;this.price=price;this.paid=paid;this.note=note;}
}
