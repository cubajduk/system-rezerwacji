package pl.fabrykaterapii.reservations.domain;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity @Table(name = "clients", indexes = {@Index(name = "idx_client_email", columnList = "email"), @Index(name = "idx_client_phone", columnList = "phone")})
public class Client {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id;
    @Column(nullable = false) private String firstName;
    @Column(nullable = false) private String lastName;
    @Column(nullable = false) private String phone;
    private String email; private LocalDate birthDate;
    @Column(nullable = false) private boolean personalDataConsent;
    @Column(nullable = false) private boolean communicationConsent;
    @Column(length = 2000) private String administrativeNote;
    protected Client() {}
    public Client(String firstName, String lastName, String phone, String email, LocalDate birthDate, boolean personalDataConsent, boolean communicationConsent, String administrativeNote) { this.firstName=firstName; this.lastName=lastName; this.phone=phone; this.email=email; this.birthDate=birthDate; this.personalDataConsent=personalDataConsent; this.communicationConsent=communicationConsent; this.administrativeNote=administrativeNote; }
    public String getId(){return id;} public String getFirstName(){return firstName;} public String getLastName(){return lastName;} public String getPhone(){return phone;} public String getEmail(){return email;} public LocalDate getBirthDate(){return birthDate;} public boolean isPersonalDataConsent(){return personalDataConsent;} public boolean isCommunicationConsent(){return communicationConsent;} public String getAdministrativeNote(){return administrativeNote;}
    public void update(String firstName,String lastName,String phone,String email,LocalDate birthDate,boolean personalDataConsent,boolean communicationConsent,String administrativeNote){this.firstName=firstName;this.lastName=lastName;this.phone=phone;this.email=email;this.birthDate=birthDate;this.personalDataConsent=personalDataConsent;this.communicationConsent=communicationConsent;this.administrativeNote=administrativeNote;}
}
