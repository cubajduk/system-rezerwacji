package pl.fabrykaterapii.reservations.domain;

import jakarta.persistence.*;

@Entity
public class ClinicSettings {
 @Id private Long id = 1L;
 @Lob @Column(nullable=false) private String configuration = "{}";
 public String getConfiguration(){return configuration;}
 public void setConfiguration(String value){configuration=value;}
}
