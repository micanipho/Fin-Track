package za.co.fintrack.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.fintrack.mappers.Mapper;
import za.co.fintrack.models.dtos.AccountDto;
import za.co.fintrack.models.entities.Account;
import za.co.fintrack.services.AccountService;

import java.util.Optional;

@RestController
@RequestMapping(path = "api/v1/accounts")
public class AccountController {

    private final AccountService accountService;
    private final Mapper<Account, AccountDto> accountDtoMapper;

    @Autowired
    public AccountController(AccountService accountService, Mapper<Account, AccountDto> accountDtoMapper) {
        this.accountService = accountService;
        this.accountDtoMapper = accountDtoMapper;
    }

    @PostMapping
    public ResponseEntity<AccountDto> createAccount(@RequestBody AccountDto accountDto) {
        Account account = accountDtoMapper.mapFrom(accountDto);
        Account savedAccount = accountService.saveAccount(account);
        return new ResponseEntity<>(accountDtoMapper.mapTo(savedAccount), HttpStatus.CREATED);
    }

    @GetMapping
    public Page<AccountDto> getAllAccounts(Pageable pageable){
        Page<Account> accounts = accountService.findAll(pageable);
        return accounts.map(accountDtoMapper::mapTo);
    }

    @GetMapping(path = "/{id}")
    public ResponseEntity<AccountDto> getAccountById(@PathVariable Long id){
        Optional<Account> account = accountService.findById(id);
        return account.map(account1 -> {
            AccountDto accountDto = accountDtoMapper.mapTo(account1);
            return new ResponseEntity<>(accountDto, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<AccountDto> deleteAccountById(@PathVariable Long id){
        if (!accountService.isExists(id)) { // Fixed: inverted logic
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        accountService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(path = "/{id}")
    public ResponseEntity<AccountDto> updateAccount(@PathVariable Long id,
                                                         @RequestBody AccountDto accountDto){
        if(!accountService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        // Get the existing account to preserve the user association
        Optional<Account> existingAccountOpt = accountService.findById(id);
        if (existingAccountOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        accountDto.setId(id);
        Account accountToUpdate = accountDtoMapper.mapFrom(accountDto);

        // If user is null in the mapped account, preserve the existing user
        if (accountToUpdate.getUser() == null) {
            accountToUpdate.setUser(existingAccountOpt.get().getUser());
        }

        Account updatedAccount = accountService.saveAccount(accountToUpdate);
        return new ResponseEntity<>(accountDtoMapper.mapTo(updatedAccount), HttpStatus.OK);
    }

    @PatchMapping(path = "/{id}")
    public ResponseEntity<AccountDto> partialUpdate(
            @PathVariable Long id,
            @RequestBody AccountDto accountDto
    ){
        if (!accountService.isExists(id)) { // Fixed: inverted logic
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Account account = accountDtoMapper.mapFrom(accountDto);
        Account updatedAccount = accountService.partialUpdate(id, account);
        return new ResponseEntity<>(accountDtoMapper.mapTo(updatedAccount), HttpStatus.OK);
    }
}
