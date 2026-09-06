package pl.fabrykaterapii.reservations.domain;

import jakarta.persistence.*;

@Entity @Table(name = "rooms")
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false, unique = true) private String name;
    @Column(length = 2000) private String equipment;
    @Column(nullable = false) private boolean active = true;
    @Version private long version;
    protected Room() {}
    public Room(String name, String equipment) {this.name=name;this.equipment=equipment;}
    public String getId(){return id;} public String getName(){return name;} public String getEquipment(){return equipment;} public boolean isActive(){return active;}
}
