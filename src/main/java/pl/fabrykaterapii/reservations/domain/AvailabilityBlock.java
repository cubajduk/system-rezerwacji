package pl.fabrykaterapii.reservations.domain;

import jakarta.persistence.*; import java.time.LocalDateTime;
@Entity @Table(name="availability_blocks")
public class AvailabilityBlock {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private String id;
    @ManyToOne(fetch=FetchType.LAZY) private Specialist specialist;
    @ManyToOne(fetch=FetchType.LAZY) private Room room;
    @Column(nullable=false) private LocalDateTime startsAt; @Column(nullable=false) private LocalDateTime endsAt;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private BlockType type; private String reason;
    protected AvailabilityBlock(){} public AvailabilityBlock(Specialist specialist, Room room, LocalDateTime startsAt, LocalDateTime endsAt, BlockType type, String reason){this.specialist=specialist;this.room=room;this.startsAt=startsAt;this.endsAt=endsAt;this.type=type;this.reason=reason;}
    public String getId(){return id;} public Specialist getSpecialist(){return specialist;} public Room getRoom(){return room;} public LocalDateTime getStartsAt(){return startsAt;} public LocalDateTime getEndsAt(){return endsAt;} public BlockType getType(){return type;} public String getReason(){return reason;}
}
