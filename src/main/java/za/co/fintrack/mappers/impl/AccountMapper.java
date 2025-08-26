package za.co.fintrack.mappers.impl;


import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import za.co.fintrack.mappers.Mapper;
import za.co.fintrack.models.dtos.AccountDto;
import za.co.fintrack.models.entities.Account;

@Component
public class AccountMapper implements Mapper<Account, AccountDto> {

    private final ModelMapper modelMapper;

    public AccountMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public AccountDto mapTo(Account account) {
        return modelMapper.map(account, AccountDto.class);
    }

    @Override
    public Account mapFrom(AccountDto accountDto) {
        return modelMapper.map(accountDto, Account.class);
    }
}
