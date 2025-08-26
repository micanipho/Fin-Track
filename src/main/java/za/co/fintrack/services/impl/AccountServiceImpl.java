package za.co.fintrack.services.impl;

import org.springframework.stereotype.Service;
import za.co.fintrack.models.entities.Account;
import za.co.fintrack.repositories.AccountRepository;
import za.co.fintrack.services.AccountService;

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
}
