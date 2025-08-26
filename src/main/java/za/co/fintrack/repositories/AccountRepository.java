package za.co.fintrack.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import za.co.fintrack.models.entities.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

}
