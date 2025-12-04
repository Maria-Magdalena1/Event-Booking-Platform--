package main.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import main.entities.Role;
import main.entities.User;
import main.exceptions.EmailRegisteredException;
import main.exceptions.UserNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import main.exceptions.UsernameTakenException;
import main.security.UserData;
import main.web.dto.UserAnalyticsDTO;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import main.repositories.UserRepository;
import main.web.dto.UserDTO;
import main.web.dto.UserRegistrationDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public User register(UserRegistrationDTO userDTO) {
        log.info("Attempting to register user with username: {}", userDTO.getUsername());
        if (isUsernameTaken(userDTO.getUsername())) {
            log.warn("Registration failed: Username {} is already taken", userDTO.getUsername());
            throw new UsernameTakenException("Username is already taken");
        }

        if (isEmailTaken(userDTO.getEmail())) {
            log.warn("Registration failed: Email {} is already registered", userDTO.getEmail());
            throw new EmailRegisteredException("Email is already registered");
        }
        User user = User.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .role(userRepository.count() == 0 ? Role.ADMIN : Role.USER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        save(user);

        log.info("User registered successfully with id: {}", user.getId());
        return user;
    }

    public UserDTO mapToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setName(user.getName());
        userDTO.setAge(user.getAge());
        userDTO.setRole(String.valueOf(user.getRole()));
        userDTO.setJoinedAt(user.getCreatedAt());
        return userDTO;
    }

    public UserAnalyticsDTO mapToAnalyticDTO(User user) {
        UserAnalyticsDTO userAnalyticsDTO = new UserAnalyticsDTO();
        userAnalyticsDTO.setId(user.getId());
        userAnalyticsDTO.setUsername(user.getUsername());

        return userAnalyticsDTO;
    }

    @Transactional
    public void update(UUID id, @Valid UserDTO userDTO) {
        log.info("Updating user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Update failed: User with id {} not found", id);
                    return new UserNotFoundException("User not found");
                });

        user.setUsername(userDTO.getUsername());
        user.setName(userDTO.getName());
        user.setAge(userDTO.getAge());
        save(user);
        log.info("User with id {} updated successfully", id);
    }

    public void toggleBlockUser(UUID id) {
        User user = findById(id);
        log.info("Toggling block status for user with id: {}", id);

        if (user.getRole() == Role.ADMIN) {
            log.warn("Cannot block/unblock admin user with id: {}", id);
            return;
        }

        user.setActive(!user.getActive());
        save(user);
        log.info("User with id {} is now {}", id, user.getActive() ? "active" : "blocked");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("Loading user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Username {} not found", username);
                    return new UsernameNotFoundException("Username not found");
                });

        if (!user.getActive()) {
            log.warn("User {} is blocked", username);
            throw new UsernameNotFoundException("This account is blocked!");
        }

        log.info("User {} loaded successfully", username);
        return new UserData(user.getId(), username, user.getPassword(), user.getRole(), user.getActive());

    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

}
