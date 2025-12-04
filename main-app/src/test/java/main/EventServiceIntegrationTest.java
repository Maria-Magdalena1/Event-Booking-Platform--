package main;

import jakarta.transaction.Transactional;
import main.entities.Event;
import main.entities.Role;
import main.entities.User;
import main.repositories.EventRepository;
import main.repositories.UserRepository;
import main.services.EventService;
import main.web.dto.EditEventDTO;
import main.web.dto.EventAnalyticsDTO;
import main.web.dto.EventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class EventServiceIntegrationTest {
    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private User user;
    private User adminUser;
    private Event event;

    @BeforeEach
    void setup() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        user = new User();
        user.setRole(Role.USER);
        user.setUsername("user123");
        user.setEmail("user123@test.com");
        user.setPassword("password123");
        user = userRepository.save(user);

        adminUser = new User();
        adminUser.setRole(Role.ADMIN);
        adminUser.setUsername("admin123");
        adminUser.setEmail("admin123@test.com");
        adminUser.setPassword("adminpass");
        adminUser = userRepository.save(adminUser);

        event = Event.builder()
                .name("Test Event")
                .description("Description")
                .venue("Hall")
                .location("City")
                .price(50.0)
                .totalSeats(100)
                .availableSeats(100)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .creator(user)
                .build();
    }

    @Test
    void save_and_findAll() {
        eventService.save(event);

        List<Event> events = eventService.findUpcomingEvents();
        assertEquals(1, events.size());
        assertEquals(event.getName(), events.get(0).getName());
    }

    @Test
    void create_event_success() {
        EventDTO dto = EventDTO.builder()
                .name("New Event")
                .description("Desc")
                .venue("Venue")
                .location("City")
                .price(20.0)
                .totalSeats(50)
                .availableSeats(50)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();

        Event created = eventService.create(dto, user);
        assertNotNull(created.getId());
        assertEquals(user.getId(), created.getCreator().getId());
    }

    @Test
    void create_event_invalidDates_throws() {
        EventDTO dto = EventDTO.builder()
                .startDate(LocalDateTime.now().plusDays(3))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        assertThrows(IllegalArgumentException.class, () -> eventService.create(dto, user));
    }

    @Test
    void create_event_invalidFields_throws() {
        EventDTO dto = EventDTO.builder()
                .startDate(LocalDateTime.now().plusDays(3))
                .endDate(LocalDateTime.now().plusDays(1))
                .name("Test")
                .description("Test desc")
                .venue("Venue")
                .location("City")
                .price(10.0)
                .totalSeats(10)
                .availableSeats(10)
                .build();

        assertThrows(IllegalArgumentException.class, () -> eventService.create(dto, user));
    }

    @Test
    void update_event_success() {
        eventService.save(event);

        EditEventDTO dto = EditEventDTO.builder()
                .name("Updated Name")
                .description("Updated Desc")
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .venue("Updated Venue")
                .location("Updated City")
                .price(60.0)
                .build();

        eventService.update(event.getId(), dto);

        Event updated = eventService.findById(event.getId());
        assertEquals("Updated Name", updated.getName());
        assertEquals(60.0, updated.getPrice());
    }

    @Test
    void update_event_invalidDates_throws() {
        eventService.save(event);

        EditEventDTO dto = EditEventDTO.builder()
                .startDate(LocalDateTime.now().plusDays(3))
                .endDate(LocalDateTime.now().plusDays(1))
                .name("Updated Name")
                .description("Updated Desc")
                .venue("Updated Venue")
                .location("Updated City")
                .price(100.0)
                .build();

        assertThrows(IllegalArgumentException.class, () -> eventService.update(event.getId(), dto));
    }

    @Test
    void update_event_invalidId_throws() {
        EditEventDTO dto = EditEventDTO.builder().build();
        assertThrows(RuntimeException.class, () -> eventService.update(UUID.randomUUID(), dto));
    }

    @Test
    void delete_event_asAdmin_success() throws AccessDeniedException {
        event.setCreator(adminUser);
        eventService.save(event);

        eventService.delete(event.getId(), adminUser);

        Event deletedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertTrue(deletedEvent.isArchived(), "Event should be archived");
    }

    @Test
    void delete_event_asOwner_success() throws AccessDeniedException {
        event.setCreator(user);
        eventService.save(event);

        eventService.delete(event.getId(), user);

        Event deletedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertTrue(deletedEvent.isArchived(), "Event should be archived");
    }

    @Test
    void delete_event_notAllowed_throws() {
        eventService.save(event);

        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setRole(Role.USER);

        assertThrows(AccessDeniedException.class, () -> eventService.delete(event.getId(), otherUser));
    }

    @Test
    void findEventsByCreator_returnsList() {
        eventService.save(event);

        List<Event> list = eventService.findEventsByCreator(user);
        assertEquals(1, list.size());
        assertEquals(user.getId(), list.get(0).getCreator().getId());
    }

    @Test
    void mapToDTO_and_mapToAnalytics() {
        EventDTO dto = eventService.mapToDTO(event);
        assertEquals(event.getName(), dto.getName());
        assertNotNull(dto.getCreator());

        EventAnalyticsDTO analytics = eventService.mapToAnalytics(event);
        assertEquals(event.getName(), analytics.getName());
        assertEquals(event.getPrice(), analytics.getPrice());
    }

    @Test
    void getUpcomingEvents_and_removeExpiredEvents() {
        Event futureEvent = new Event();
        futureEvent.setName("Future Event");
        futureEvent.setDescription("Future Event");
        futureEvent.setVenue("Future Venue");
        futureEvent.setLocation("Future City");
        futureEvent.setPrice(50.0);
        futureEvent.setStartDate(LocalDateTime.now().plusDays(1));
        futureEvent.setEndDate(LocalDateTime.now().plusDays(2));
        futureEvent.setTotalSeats(100);
        futureEvent.setAvailableSeats(100);
        eventService.save(futureEvent);

        Event pastEvent = new Event();
        pastEvent.setName("Past Event");
        pastEvent.setDescription("Past Event");
        pastEvent.setVenue("Past Venue");
        pastEvent.setLocation("Past City");
        pastEvent.setPrice(50.0);
        pastEvent.setStartDate(LocalDateTime.now().minusDays(2));
        pastEvent.setEndDate(LocalDateTime.now().minusDays(1));
        pastEvent.setTotalSeats(50);
        pastEvent.setAvailableSeats(50);
        eventService.save(pastEvent);

        eventService.removeExpiredEvents();

        List<Event> upcomingEvents = eventService.getUpcomingEvents();

        assertThat(upcomingEvents)
                .hasSize(1)
                .extracting(Event::getName)
                .containsExactly("Future Event");
    }

    @Test
    void findById_notFound_throws() {
        UUID id = UUID.randomUUID();
        assertThrows(RuntimeException.class, () -> eventService.findById(id));
    }
}
