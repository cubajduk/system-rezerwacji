package pl.fabrykaterapii.reservations.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class CatalogImport {
    @Id private String id;
    protected CatalogImport() {}
    public CatalogImport(String id) { this.id = id; }
}
