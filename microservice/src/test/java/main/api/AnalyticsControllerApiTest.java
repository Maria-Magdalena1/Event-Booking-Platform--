package main.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.web.AnalyticsController;
import main.entity.Booking;
import main.entity.Event;
import main.entity.User;
import main.service.BookingService;
import main.service.EventService;
import main.service.UserService;
import main.web.dto.BookingDTO;
import main.web.dto.EventDTO;
import main.web.dto.UserDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AnalyticsController.class)
@Import(AnalyticsControllerApiTest.MockConfig.class)
public class AnalyticsControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private EventService eventService;

    @Autowired
    private BookingService bookingService;

    private ObjectMapper objectMapper;
    private UUID userId;
    private UUID eventId;
    private UUID bookingId;


    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper();
        userId = UUID.randomUUID();
        eventId = UUID.randomUUID();
        bookingId = UUID.randomUUID();

    }

    @TestConfiguration
    static class MockConfig {

        @Bean
        UserService userService() {
            return Mockito.mock(UserService.class);
        }

        @Bean
        EventService eventService() {
            return Mockito.mock(EventService.class);
        }

        @Bean
        BookingService bookingService() {
            return Mockito.mock(BookingService.class);
        }
    }

    @Test
    void testGetDashboard() throws Exception {
        Mockito.when(userService.count()).thenReturn(1L);
        Mockito.when(eventService.count()).thenReturn(1L);
        Mockito.when(bookingService.count()).thenReturn(1L);
        Mockito.when(bookingService.findAll()).thenReturn(Collections.emptyList());
        Mockito.when(eventService.findAll()).thenReturn(Collections.emptyList());
        Mockito.when(userService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(1))
                .andExpect(jsonPath("$.totalEvents").value(1))
                .andExpect(jsonPath("$.totalBookings").value(1))
                .andExpect(jsonPath("$.totalRevenue").value(0))
                .andExpect(jsonPath("$.topEvents").isArray())
                .andExpect(jsonPath("$.seatWarnings").isArray());
    }

    @Test
    void testGetDashboardWithData_fullCoverage() throws Exception {
        UUID user1 = UUID.randomUUID();
        UUID user2 = UUID.randomUUID();

        User u1 = new User(user1, "Alice");
        User u2 = new User(user2, "Bob");

        UUID event1 = UUID.randomUUID();
        UUID event2 = UUID.randomUUID();

        Event e1 = new Event(event1, "Concert", 100, 50.0);
        Event e2 = new Event(event2, "Workshop", 10, 20.0);

        Booking b1 = new Booking(UUID.randomUUID(), event1, user1, 3, 150.0);
        Booking b2 = new Booking(UUID.randomUUID(), event1, user2, 5, 250.0);
        Booking b3 = new Booking(UUID.randomUUID(), event2, user1, 6, 120.0);

        Mockito.when(userService.count()).thenReturn(2L);
        Mockito.when(eventService.count()).thenReturn(2L);
        Mockito.when(bookingService.count()).thenReturn(3L);

        Mockito.when(userService.findAll()).thenReturn(List.of(u1, u2));
        Mockito.when(eventService.findAll()).thenReturn(List.of(e1, e2));
        Mockito.when(bookingService.findAll()).thenReturn(List.of(b1, b2, b3));

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())

                .andExpect(jsonPath("$.totalUsers").value(2))
                .andExpect(jsonPath("$.totalEvents").value(2))
                .andExpect(jsonPath("$.totalBookings").value(3))
                .andExpect(jsonPath("$.totalRevenue").value(520.0))

                .andExpect(jsonPath("$.topEvents[0].name").value("Concert"))
                .andExpect(jsonPath("$.topEvents[0].totalSeatsBooked").value(8))
                .andExpect(jsonPath("$.seatWarnings[0].eventName").value("Workshop"))
                .andExpect(jsonPath("$.seatWarnings[0].freeSeats").value(4));
    }

    @Test
    void testAddUser() throws Exception {
        UserDTO dto = new UserDTO(userId, "testUser");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Mockito.verify(userService).save(any(User.class));
    }

    @Test
    void testGetAllUsers() throws Exception {
        User user = new User(userId, "testUser");
        Mockito.when(userService.findAll()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userId.toString()))
                .andExpect(jsonPath("$[0].username").value("testUser"));
    }

    @Test
    void testAddEvent() throws Exception {
        EventDTO dto = new EventDTO(eventId, "Concert", 100, 50.0);

        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Mockito.verify(eventService).save(any(Event.class));
    }

    @Test
    void testGetAllEvents() throws Exception {
        Event event = new Event(eventId, "Concert", 100, 50.0);
        Mockito.when(eventService.findAll()).thenReturn(List.of(event));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventId.toString()))
                .andExpect(jsonPath("$[0].name").value("Concert"))
                .andExpect(jsonPath("$[0].totalSeats").value(100))
                .andExpect(jsonPath("$[0].price").value(50.0));
    }

    @Test
    void testConfirmBooking() throws Exception {
        BookingDTO dto = new BookingDTO(bookingId, eventId, userId, 2, 100.0);

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

        Mockito.verify(bookingService).save(any(Booking.class));
    }

    @Test
    void testGetAllBookings() throws Exception {
        Booking booking = new Booking(bookingId, eventId, userId, 2, 100.0);
        Mockito.when(bookingService.findAll()).thenReturn(List.of(booking));

        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bookingId.toString()))
                .andExpect(jsonPath("$[0].eventId").value(eventId.toString()))
                .andExpect(jsonPath("$[0].userId").value(userId.toString()))
                .andExpect(jsonPath("$[0].seatsBooked").value(2))
                .andExpect(jsonPath("$[0].price").value(100.0));
    }
}
