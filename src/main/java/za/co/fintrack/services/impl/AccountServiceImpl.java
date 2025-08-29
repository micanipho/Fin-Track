package za.co.fintrack.services.impl;

import org.springframework.stereotype.Service;
import za.co.fintrack.models.entities.Account;
import za.co.fintrack.repositories.AccountRepository;
import za.co.fintrack.services.AccountService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account saveAccount(Account account) {
        return accountRepository.save(account);
    }

    @Override
    public List<Account> findAll() {
        return new ArrayList<>(accountRepository.findAll());
    }

    @Override
    public Optional<Account> findById(Long id) {
        return accountRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        accountRepository.deleteById(id);
    }

    @Override
    public boolean isExists(Long id) {
        return accountRepository.existsById(id);
    }

    @Override
    public Account partialUpdate(Long id, Account account) {
        account.setId(id);

        return accountRepository.findById(id).map(existingAccount -> {
            Optional.ofNullable(account.getType()).ifPresent(existingAccount::setType);
            Optional.ofNullable(account.getStatus()).ifPresent(existingAccount::setStatus);
            Optional.ofNullable(account.getUser()).ifPresent(existingAccount::setUser);
            Optional.ofNullable(account.getBalance()).ifPresent(existingAccount::setBalance);
            Optional.ofNullable(account.getName()).ifPresent(existingAccount::setName);

            accountRepository.save(existingAccount);
            return  existingAccount;
        }).orElseThrow(() -> new RuntimeException("Account does not exist"));
    }
}
