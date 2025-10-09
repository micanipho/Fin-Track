package za.co.fintrack.mappers.impl;

import org.springframework.stereotype.Component;
import za.co.fintrack.mappers.Mapper;
import za.co.fintrack.models.dtos.UserProfileDto;
import za.co.fintrack.models.entities.User;

@Component
public class UserProfileMapper implements Mapper<User, UserProfileDto> {

    @Override
    public UserProfileDto mapTo(User user) {
        return UserProfileDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .profilePictureUrl(user.getProfilePictureUrl())
                .emailVerified(user.isEmailVerified())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    @Override
    public User mapFrom(UserProfileDto userProfileDto) {
        return User.builder()
                .id(userProfileDto.getId())
                .username(userProfileDto.getUsername())
                .email(userProfileDto.getEmail())
                .firstName(userProfileDto.getFirstName())
                .lastName(userProfileDto.getLastName())
                .phoneNumber(userProfileDto.getPhoneNumber())
                .profilePictureUrl(userProfileDto.getProfilePictureUrl())
                .build();
    }
}
