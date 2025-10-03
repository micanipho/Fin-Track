package za.co.fintrack.services;

import org.springframework.security.core.userdetails.UserDetails;
import za.co.fintrack.models.dtos.UserDto;

public interface AuthenticationService {

    UserDetails authenticate(String username, String password);
    String generateToken(UserDetails userDetails);
    UserDetails validateToken(String token);
    UserDto register(UserDto userDto);
}
