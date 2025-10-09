package za.co.fintrack.mappers.impl;

import org.springframework.stereotype.Component;
import za.co.fintrack.mappers.Mapper;
import za.co.fintrack.models.dtos.UserDto;
import za.co.fintrack.models.entities.User;

import java.time.LocalDateTime;

@Component
public class UserMapper implements Mapper<User, UserDto> {

    @Override
    public UserDto mapTo(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .role(user.getRole())
                .active(user.isActive())
                .build();
    }

    @Override
    public User mapFrom(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .username(userDto.getUsername())
                .email(userDto.getEmail())
                .password(userDto.getPassword())
                .role(userDto.getRole())
                .active(userDto.isActive())
                .emailVerified(false) // Default for new users
                .failedLoginAttempts(0) // Default
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
