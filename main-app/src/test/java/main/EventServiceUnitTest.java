package main;

import main.entities.Event;
import main.entities.Role;
import main.entities.User;
import main.exceptions.EventNotFoundException;
import main.repositories.EventRepository;
import main.services.EventService;
import main.web.dto.EditEventDTO;
import main.web.dto.EventAnalyticsDTO;
import main.web.dto.EventDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EventServiceUnitTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    private User user;
    private Event event;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setRole(Role.USER);

        event = new Event();
        event.setId(UUID.randomUUID());
        event.setName("Test Event");
        event.setCreator(user);
        event.setAvailableSeats(10);

    }

    @Test
    void testFindById_EventExists() {
        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));

        Event found = eventService.findById(event.getId());

        assertEquals(event.getId(), found.getId());
    }

    @Test
    void testFindById_NotFound() {
        UUID id = UUID.randomUUID();

        Mockito.when(eventRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class,
                () -> eventService.findById(id));
    }

    @Test
    void testFindUpcomingEvents() {
        Mockito.when(eventRepository.findAllUpcoming(Mockito.any()))
                .thenReturn(Collections.singletonList(event));

        List<Event> upcoming = eventService.findUpcomingEvents();

        assertFalse(upcoming.isEmpty());
        assertEquals(event.getName(), upcoming.get(0).getName());
    }

    @Test
    void testCreate_ValidEvent() {
        EventDTO dto = EventDTO.builder()
                .name("Test Event")
                .description("Desc")
                .venue("Hall 1")
                .location("Sofia")
                .price(10.0)
                .totalSeats(50)
                .startDate(LocalDateTime.now().plusDays(1))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();

        Mockito.when(eventRepository.save(Mockito.any(Event.class)))
                .thenAnswer(i -> i.getArgument(0));

        Event created = eventService.create(dto, user);

        assertEquals(dto.getName(), created.getName());
        assertEquals(user, created.getCreator());
    }

    @Test
    void testCreate_InvalidDates() {
        EventDTO dto = EventDTO.builder()
                .startDate(LocalDateTime.now().plusDays(2))
                .endDate(LocalDateTime.now().plusDays(1))
                .build();

        assertThrows(IllegalArgumentException.class, () -> eventService.create(dto, user));
    }

    @Test
    void testUpdate_EventExists() {
        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));

        EditEventDTO dto = EditEventDTO.builder()
                .name("Updated Name")
                .description("Updated Desc")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .venue("Updated Venue")
                .location("Updated Location")
                .price(30.0)
                .build();

        eventService.update(event.getId(), dto);

        assertEquals("Updated Name", event.getName());
        Mockito.verify(eventRepository).save(event);
    }

    @Test
    void testUpdate_NotFound() {
        UUID id = UUID.randomUUID();

        Mockito.when(eventRepository.findById(id))
                .thenReturn(Optional.empty());

        EditEventDTO dto = EditEventDTO.builder()
                .name("New Name")
                .description("New Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .venue("New Venue")
                .location("New Location")
                .price(20.0)
                .build();

        assertThrows(EventNotFoundException.class, () -> eventService.update(id, dto));
    }

    @Test
    void testMapToDTO() {
        EventDTO dto = eventService.mapToDTO(event);

        assertEquals(event.getId(), dto.getId());
        assertEquals(event.getName(), dto.getName());
        assertNotNull(dto.getCreator());
        assertEquals(user.getId(), dto.getCreator().getId());
    }

    @Test
    void testDelete_AsAdmin() throws AccessDeniedException {
        User admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(Role.ADMIN);

        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));

        eventService.delete(event.getId(), admin);

        Mockito.verify(eventRepository).save(Mockito.argThat(Event::isArchived));
    }

    @Test
    void testDelete_AsOwner_Active() throws AccessDeniedException {
        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));

        eventService.delete(event.getId(), user);

        Mockito.verify(eventRepository).save(Mockito.argThat(Event::isArchived));
    }

    @Test
    void testDelete_NotAllowed() {
        User otherUser = new User();
        otherUser.setId(UUID.randomUUID());
        otherUser.setRole(Role.USER);

        Mockito.when(eventRepository.findById(event.getId()))
                .thenReturn(Optional.of(event));

        assertThrows(AccessDeniedException.class,
                () -> eventService.delete(event.getId(), otherUser));
    }

    @Test
    void testFindEventsByCreator() {
        Mockito.when(eventRepository.findAllByCreator(user))
                .thenReturn(Collections.singletonList(event));

        List<Event> list = eventService.findEventsByCreator(user);

        assertFalse(list.isEmpty());
        assertEquals(user, list.get(0).getCreator());
    }

    @Test
    void testGetUpcomingEvents() {
        Mockito.when(eventRepository.findAllUpcomingNotArchived(Mockito.any()))
                .thenReturn(Collections.singletonList(event));

        eventService.getUpcomingEvents();

        Mockito.verify(eventRepository).findAllUpcomingNotArchived(Mockito.any());
    }

    @Test
    void testRemoveExpiredEvents() {
        Event expiredEvent = new Event();
        expiredEvent.setArchived(false);
        expiredEvent.setEndDate(LocalDateTime.now().minusDays(1));

        Mockito.when(eventRepository.findAllByEndDateBeforeAndArchivedFalse(Mockito.any()))
                .thenReturn(List.of(expiredEvent));

        eventService.removeExpiredEvents();

        Mockito.verify(eventRepository).saveAll(Mockito.argThat(list ->
                {
                    boolean[] allArchived = {true};
                    list.forEach(e -> {
                        if (!e.isArchived()) {
                            allArchived[0] = false;
                        }
                    });
                    return allArchived[0];
                }
        ));
    }

    @Test
    void testMapToAnalytics() {
        EventAnalyticsDTO dto = eventService.mapToAnalytics(event);

        assertEquals(event.getId(), dto.getId());
        assertEquals(event.getName(), dto.getName());
        assertEquals(event.getPrice(), dto.getPrice());
    }
}
