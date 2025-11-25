package main.service;

import main.entity.User;
import main.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void save_callsRepositorySave() {
        User user = new User();

        userService.save(user);

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void findAll_returnsListFromRepository() {
        User user1 = new User();
        User user2 = new User();
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(users, result);

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void count_returnsRepositoryCount() {
        long expectedCount = 5L;
        when(userRepository.count()).thenReturn(expectedCount);

        Object result = userService.count();

        assertEquals(expectedCount, result);
        verify(userRepository, times(1)).count();
    }
}
