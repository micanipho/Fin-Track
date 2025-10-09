package za.co.fintrack.services;

import org.springframework.security.core.userdetails.UserDetails;
import za.co.fintrack.models.dtos.UserDto;

public interface AuthenticationService {

    UserDetails authenticate(String username, String password);
    String generateToken(UserDetails userDetails);
    String generateRefreshToken(UserDetails userDetails);
    UserDetails validateToken(String token);
    UserDetails refreshToken(String refreshToken);
    UserDto register(UserDto userDto);
    void logout(String token);
    boolean isTokenBlacklisted(String token);
}
