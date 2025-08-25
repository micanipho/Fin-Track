package za.co.fintrack.mappers.impl;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import za.co.fintrack.mappers.Mapper;
import za.co.fintrack.models.dtos.UserDto;
import za.co.fintrack.models.entities.User;

@Component
public class UserMapper implements Mapper<User, UserDto> {

    private final ModelMapper modelMapper;

    public UserMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    @Override
    public UserDto mapTo(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User mapFrom(UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }
}
