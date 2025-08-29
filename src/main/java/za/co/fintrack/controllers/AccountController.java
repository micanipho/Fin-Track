package za.co.fintrack.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.co.fintrack.mappers.Mapper;
import za.co.fintrack.models.dtos.AccountDto;
import za.co.fintrack.models.entities.Account;
import za.co.fintrack.services.AccountService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<AccountDto> getAllAccounts(){
        List<Account> accounts = accountService.findAll();
        return accounts.stream()
                .map(accountDtoMapper::mapTo)
                .collect(Collectors.toList());
    }

    @GetMapping(path = "/{id}")
    public  ResponseEntity<AccountDto> getAccountById(@PathVariable Long id){
        Optional<Account> account = accountService.findById(id);
        return account.map(account1 -> {
            AccountDto accountDto = accountDtoMapper.mapTo(account1);
            return new  ResponseEntity<>(accountDto, HttpStatus.OK);
        }).orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<AccountDto> deleteAccountById(@PathVariable Long id){
        if (!accountService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        accountService.deleteById(id);
        return  new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(path = "/{id}")
    public  ResponseEntity<AccountDto> updateAccount(@PathVariable Long id,
                                                         @RequestBody AccountDto accountDto){
        if(!accountService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        accountDto.setId(id);
        Account updatedAccount = accountService.saveAccount(accountDtoMapper.mapFrom(accountDto));
        return new ResponseEntity<>(accountDtoMapper.mapTo(updatedAccount), HttpStatus.OK);
    }

    @PatchMapping(path = "/{id}")
    public ResponseEntity<AccountDto> partialUpdate(
            @PathVariable Long id,
            @RequestBody AccountDto accountDto
    ){
        if (!accountService.isExists(id)) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        Account account = accountDtoMapper.mapFrom(accountDto);
        Account updatedAccount = accountService.partialUpdate(id, account);
        return new  ResponseEntity<>(accountDtoMapper.mapTo(updatedAccount), HttpStatus.OK);
    }
}
