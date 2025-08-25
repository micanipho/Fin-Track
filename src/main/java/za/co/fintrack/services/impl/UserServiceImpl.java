package za.co.fintrack.services.impl;

import org.springframework.stereotype.Service;
import za.co.fintrack.models.entities.User;
import za.co.fintrack.repositories.UserRepository;
import za.co.fintrack.services.UserService;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
