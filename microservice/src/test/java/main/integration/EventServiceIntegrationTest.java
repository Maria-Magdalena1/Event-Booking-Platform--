package main.integration;

import main.entity.Event;
import main.service.EventService;
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
public class EventServiceIntegrationTest {
    @Autowired
    private EventService eventService;

    @Test
    void save_event_isPersisted() {
        Event event = Event.builder()
                .id(UUID.randomUUID())
                .name("Concert")
                .totalSeats(100)
                .price(50.0)
                .build();

        eventService.save(event);

        List<Event> events = eventService.findAll();
        assertEquals(1, events.size());
        assertEquals("Concert", events.get(0).getName());
        assertEquals(100, events.get(0).getTotalSeats());
        assertEquals(50.0, events.get(0).getPrice());
    }

    @Test
    void findAll_returnsAllEvents() {
        Event event1 = Event.builder().id(UUID.randomUUID()).name("Concert").totalSeats(100).price(50.0).build();
        Event event2 = Event.builder().id(UUID.randomUUID()).name("Theater").totalSeats(50).price(30.0).build();

        eventService.save(event1);
        eventService.save(event2);

        List<Event> events = eventService.findAll();
        assertEquals(2, events.size());
        assertTrue(events.stream().anyMatch(e -> e.getName().equals("Concert")));
        assertTrue(events.stream().anyMatch(e -> e.getName().equals("Theater")));
    }

    @Test
    void findAll_emptyList() {
        List<Event> events = eventService.findAll();
        assertTrue(events.isEmpty());
    }

    @Test
    void count_returnsNumberOfEvents() {
        Event event1 = Event.builder().id(UUID.randomUUID()).name("Concert").totalSeats(100).price(50.0).build();
        Event event2 = Event.builder().id(UUID.randomUUID()).name("Theater").totalSeats(50).price(30.0).build();

        eventService.save(event1);
        eventService.save(event2);

        Object count = eventService.count();
        assertEquals(2L, count);
    }

    @Test
    void count_emptyDatabase() {
        Object count = eventService.count();
        assertEquals(0L, count);
    }
}
