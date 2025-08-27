package za.co.fintrack.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import za.co.fintrack.mappers.Mapper;
import za.co.fintrack.models.dtos.AccountDto;
import za.co.fintrack.models.entities.Account;
import za.co.fintrack.services.AccountService;

@RestController
public class AccountController {

    private final AccountService accountService;
    private final Mapper<Account, AccountDto> accountDtoMapper;

    @Autowired
    public AccountController(AccountService accountService, Mapper<Account, AccountDto> accountDtoMapper) {
        this.accountService = accountService;
        this.accountDtoMapper = accountDtoMapper;
    }

    @PostMapping(path = "/accounts")
    public ResponseEntity<AccountDto> createAccount(@RequestBody AccountDto accountDto) {
        Account account = accountDtoMapper.mapFrom(accountDto);
        Account savedAccount = accountService.saveAccount(account);
        return new ResponseEntity<>(accountDtoMapper.mapTo(savedAccount), HttpStatus.CREATED);
    }

}
