package main.integration;

import main.entity.User;
import main.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Test
    void save_user_isPersisted() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .username("Alice")
                .build();

        userService.save(user);

        List<User> users = userService.findAll();
        assertEquals(1, users.size());
        assertEquals("Alice", users.get(0).getUsername());
    }

    @Test
    void findAll_returnsAllUsers() {
        User user1 = User.builder().id(UUID.randomUUID()).username("Alice").build();
        User user2 = User.builder().id(UUID.randomUUID()).username("Bob").build();

        userService.save(user1);
        userService.save(user2);

        List<User> users = userService.findAll();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("Alice")));
        assertTrue(users.stream().anyMatch(u -> u.getUsername().equals("Bob")));
    }

    @Test
    void count_returnsNumberOfUsers() {
        User user1 = User.builder().id(UUID.randomUUID()).username("Alice").build();
        User user2 = User.builder().id(UUID.randomUUID()).username("Bob").build();

        userService.save(user1);
        userService.save(user2);

        Object count = userService.count();
        assertEquals(2L, count);
    }

}
