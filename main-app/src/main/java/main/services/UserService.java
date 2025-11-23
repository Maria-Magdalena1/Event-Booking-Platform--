package main.services;

import jakarta.transaction.Transactional;
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
        if (isUsernameTaken(userDTO.getUsername())) {
            throw new UsernameTakenException("Username is already taken");
        }

        if (isEmailTaken(userDTO.getEmail())) {
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
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setUsername(userDTO.getUsername());
        user.setName(userDTO.getName());
        user.setAge(userDTO.getAge());
        save(user);
    }

    public void toggleBlockUser(UUID id) {
        User user = findById(id);
        user.setActive(!user.getActive());
        save(user);
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        //HttpSession currentSession = servletRequestAttributes.getRequest().getSession(true);
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("Username not found"));

        //if (!user.isActive()) {
        //    currentSession.setAttribute("inactiveUserMessage", "This account is blocked!");
        //}

        if (!user.getActive()) {
            throw new UsernameNotFoundException("This account is blocked!");
        }

        return new UserData(user.getId(), username, user.getPassword(), user.getRole(), user.getActive());

    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

}
