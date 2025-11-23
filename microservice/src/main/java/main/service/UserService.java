package main.service;

import lombok.RequiredArgsConstructor;
import main.entity.User;
import main.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public void save(User user) {
        userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public Object count() {
        return userRepository.count();
    }
}
