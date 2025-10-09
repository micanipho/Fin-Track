package za.co.fintrack.mappers.impl;


import org.springframework.stereotype.Component;
import za.co.fintrack.mappers.Mapper;
import za.co.fintrack.models.dtos.AccountDto;
import za.co.fintrack.models.dtos.UserDto;
import za.co.fintrack.models.entities.Account;
import za.co.fintrack.models.entities.User;
import za.co.fintrack.repositories.UserRepository;

@Component
public class AccountMapper implements Mapper<Account, AccountDto> {

    private final UserRepository userRepository;
    private final Mapper<User, UserDto> userMapper;

    public AccountMapper(UserRepository userRepository, Mapper<User, UserDto> userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public AccountDto mapTo(Account account) {
        UserDto userDto = account.getUser() != null ? userMapper.mapTo(account.getUser()) : null;

        return AccountDto.builder()
                .id(account.getId())
                .userId(account.getUser() != null ? account.getUser().getId() : null)
                .name(account.getName())
                .type(account.getType())
                .status(account.getStatus())
                .balance(account.getBalance())
                .user(userDto)
                .build();
    }

    @Override
    public Account mapFrom(AccountDto accountDto) {
        User user = null;
        if (accountDto.getUserId() != null) {
            user = userRepository.findById(accountDto.getUserId()).orElse(null);
        }

        return Account.builder()
                .id(accountDto.getId())
                .user(user)
                .name(accountDto.getName())
                .type(accountDto.getType())
                .status(accountDto.getStatus())
                .balance(accountDto.getBalance())
                .build();
    }
}
