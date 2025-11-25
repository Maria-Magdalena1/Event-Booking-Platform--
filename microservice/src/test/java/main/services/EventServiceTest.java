package main.service;

import main.entity.Event;
import main.repositories.EventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {
    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void save_callsRepositorySave() {
        Event event = new Event();

        eventService.save(event);

        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void findAll_returnsListFromRepository() {
        Event event1 = new Event();
        Event event2 = new Event();
        List<Event> events = Arrays.asList(event1, event2);

        when(eventRepository.findAll()).thenReturn(events);

        List<Event> result = eventService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(events, result);

        verify(eventRepository, times(1)).findAll();
    }

    @Test
    void count_returnsRepositoryCount() {
        long expectedCount = 7L;
        when(eventRepository.count()).thenReturn(expectedCount);

        Object result = eventService.count();

        assertEquals(expectedCount, result);
        verify(eventRepository, times(1)).count();
    }
}
