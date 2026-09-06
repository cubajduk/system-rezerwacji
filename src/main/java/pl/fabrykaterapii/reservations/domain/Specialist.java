package pl.fabrykaterapii.reservations.domain;

import jakarta.persistence.*;

@Entity @Table(name = "specialists")
public class Specialist {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String firstName; @Column(nullable = false) private String lastName;
    @Column(nullable = false) private String specialization; @Column(nullable = false) private String color = "#2563eb";
    @Column(nullable = false) private boolean active = true;
    @Version private long version;
    protected Specialist() {}
    public Specialist(String firstName, String lastName, String specialization, String color) {this.firstName=firstName;this.lastName=lastName;this.specialization=specialization;if(color!=null)this.color=color;}
    public String getId(){return id;} public String getFirstName(){return firstName;} public String getLastName(){return lastName;} public String getSpecialization(){return specialization;} public String getColor(){return color;} public boolean isActive(){return active;}
}
