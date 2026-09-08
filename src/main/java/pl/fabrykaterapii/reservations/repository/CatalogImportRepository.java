package pl.fabrykaterapii.reservations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.fabrykaterapii.reservations.domain.CatalogImport;

public interface CatalogImportRepository extends JpaRepository<CatalogImport, String> {}
