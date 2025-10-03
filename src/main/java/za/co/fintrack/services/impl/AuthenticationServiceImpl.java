package za.co.fintrack.services.impl;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import za.co.fintrack.mappers.Mapper;
import za.co.fintrack.models.dtos.UserDto;
import za.co.fintrack.models.entities.User;
import za.co.fintrack.services.AuthenticationService;
import za.co.fintrack.services.UserService;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final Mapper<User, UserDto> mapper;
    private final UserServiceImpl userService;

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    public UserDetails authenticate(String username, String password) {
        assert authenticationManager != null;
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );
        assert userDetailsService != null;
        return userDetailsService.loadUserByUsername(username);
    }



    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        long jwtExpiryMs = 8640000L;
        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiryMs))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public UserDetails validateToken(String token) {
        String userName = extractUserName(token);
        assert userDetailsService != null;
        return userDetailsService.loadUserByUsername(userName);
    }

    @Override
    public UserDto register(UserDto userDto) {

        User userToSave = mapper.mapFrom(userDto);
        User savedUser = userService.saveUser(userToSave);
        return mapper.mapTo(savedUser);
    }

    private String extractUserName(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }


    private Key getSigningKey() {
        byte[] keyBytes = secretKey.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
