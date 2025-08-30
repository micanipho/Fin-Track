package za.co.fintrack.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import za.co.fintrack.models.entities.Account;

import java.util.List;
import java.util.Optional;

public interface AccountService {

    Account saveAccount(Account account);

    List<Account> findAll();

    Page<Account> findAll(Pageable pageable);

    Optional<Account> findById(Long id);

    void deleteById(Long id);

    boolean isExists(Long id);

    Account partialUpdate(Long id, Account account);
}
