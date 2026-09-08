package pl.fabrykaterapii.reservations.domain;

import jakarta.persistence.*;

@Entity @Table(name = "rooms")
public class Room {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false, unique = true) private String name;
    @Column(length = 20) private String shortName;
    @Column(length = 2000) private String equipment;
    @Column(nullable = false) private boolean active = true;
    @Version private long version;
    protected Room() {}
    public Room(String name, String shortName, String equipment) {this.name=name;this.shortName=shortName;this.equipment=equipment;}
    public String getId(){return id;} public String getName(){return name;} public String getShortName(){return shortName==null||shortName.isBlank()?name.replaceFirst("(?i)^Gabinet\\s+", "G"):shortName;} public String getEquipment(){return equipment;} public boolean isActive(){return active;}
    public void update(String name,String shortName,String equipment){this.name=name;this.shortName=shortName;this.equipment=equipment;}
}
