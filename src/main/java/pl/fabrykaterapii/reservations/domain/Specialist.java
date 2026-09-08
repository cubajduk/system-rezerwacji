package pl.fabrykaterapii.reservations.domain;

import jakarta.persistence.*;
import java.util.*;

@Entity @Table(name = "specialists")
public class Specialist {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String firstName; @Column(nullable = false) private String lastName;
    @Column(nullable = false) private String specialization; @Column(nullable = false) private String color = "#2563eb";
    @Column(nullable = false) private boolean active = true;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "primary_room_id") private Room primaryRoom;
    @ManyToMany(fetch = FetchType.EAGER) @JoinTable(name = "specialist_alternative_rooms", joinColumns = @JoinColumn(name = "specialist_id"), inverseJoinColumns = @JoinColumn(name = "room_id")) @OrderColumn(name = "priority_order") private List<Room> alternativeRooms = new ArrayList<>();
    @Version private long version;
    protected Specialist() {}
    public Specialist(String firstName, String lastName, String specialization, String color) {this.firstName=firstName;this.lastName=lastName;this.specialization=specialization;if(color!=null)this.color=color;}
    public String getId(){return id;} public String getFirstName(){return firstName;} public String getLastName(){return lastName;} public String getSpecialization(){return specialization;} public String getColor(){return color;} public boolean isActive(){return active;} public Room getPrimaryRoom(){return primaryRoom;} public List<Room> getAlternativeRooms(){return List.copyOf(alternativeRooms);}
    public void update(String firstName,String lastName,String specialization,String color){this.firstName=firstName;this.lastName=lastName;this.specialization=specialization;if(color!=null&&!color.isBlank())this.color=color;}
    public void setRoomPreferences(Room primaryRoom,List<Room> alternatives){this.primaryRoom=primaryRoom;this.alternativeRooms.clear();this.alternativeRooms.addAll(alternatives.stream().filter(room->primaryRoom==null||!room.getId().equals(primaryRoom.getId())).toList());}
}
