package pl.fabrykaterapii.reservations.domain;

import jakarta.persistence.*; import java.math.BigDecimal;
@Entity @Table(name = "therapy_services")
public class TherapyService {
    @Column(length = 500) private String description;
    @Column(nullable = false, columnDefinition = "boolean default false") private boolean deleted;
    public String getDescription(){return description;} public boolean isDeleted(){return deleted;}
    public void delete(){deleted=true;}
    public void update(String name,Integer duration,BigDecimal price,String equipment,String description){this.name=name.trim();this.suggestedDurationMinutes=duration;this.defaultPrice=price;this.requiredEquipment=equipment;this.description=description;}
    @Id @GeneratedValue(strategy = GenerationType.UUID) private String id; @Column(nullable = false, unique = true) private String name;
    private Integer suggestedDurationMinutes; private BigDecimal defaultPrice; private String requiredEquipment;
    protected TherapyService() {} public TherapyService(String name, Integer suggestedDurationMinutes, BigDecimal defaultPrice, String requiredEquipment){this.name=name;this.suggestedDurationMinutes=suggestedDurationMinutes;this.defaultPrice=defaultPrice;this.requiredEquipment=requiredEquipment;}
    public String getId(){return id;} public String getName(){return name;} public Integer getSuggestedDurationMinutes(){return suggestedDurationMinutes;} public BigDecimal getDefaultPrice(){return defaultPrice;} public String getRequiredEquipment(){return requiredEquipment;}
}
