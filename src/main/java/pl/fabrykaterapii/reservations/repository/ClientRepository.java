package pl.fabrykaterapii.reservations.repository;
import org.springframework.data.jpa.repository.JpaRepository; import pl.fabrykaterapii.reservations.domain.Client;
import java.util.*;
public interface ClientRepository extends JpaRepository<Client,String> { List<Client> findByPhoneOrEmail(String phone, String email); }
