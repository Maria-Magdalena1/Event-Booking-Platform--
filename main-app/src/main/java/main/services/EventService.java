package main.services;

import jakarta.transaction.Transactional;
import main.entities.Booking;
import main.entities.Event;
import main.entities.User;
import main.exceptions.EventNotFoundException;
import lombok.RequiredArgsConstructor;
import main.repositories.BookingRepository;
import main.repositories.UserRepository;
import main.web.dto.EditEventDTO;
import main.web.dto.EventAnalyticsDTO;
import main.web.dto.UserDTO;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import main.repositories.EventRepository;
import main.web.dto.EventDTO;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;

    public Event findById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));
    }

    public List<Event> findUpcomingEvents() {
        return eventRepository.findAllUpcoming(LocalDateTime.now());
    }

    public void save(Event event) {
        eventRepository.save(event);
    }

    public Event create(EventDTO eventDTO, User user) {
        if (eventDTO.getStartDate().isAfter(eventDTO.getEndDate())) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        Event event = Event.builder()
                .name(eventDTO.getName())
                .description(eventDTO.getDescription())
                .startDate(eventDTO.getStartDate())
                .endDate(eventDTO.getEndDate())
                .venue(eventDTO.getVenue())
                .location(eventDTO.getLocation())
                .price(eventDTO.getPrice())
                .totalSeats(eventDTO.getTotalSeats())
                .availableSeats(eventDTO.getTotalSeats())
                .creator(user)
                .build();

        save(event);
        return event;
    }

    @Transactional
    public void update(UUID id, EditEventDTO eventDTO) {
        Event existingEvent = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        existingEvent.setName(eventDTO.getName());
        existingEvent.setDescription(eventDTO.getDescription());
        existingEvent.setStartDate(eventDTO.getStartDate());
        existingEvent.setEndDate(eventDTO.getEndDate());
        existingEvent.setVenue(eventDTO.getVenue());
        existingEvent.setLocation(eventDTO.getLocation());
        existingEvent.setPrice(eventDTO.getPrice());
        eventRepository.save(existingEvent);
    }

    public void update(Event event) {
        eventRepository.save(event);
    }

    public EventDTO mapToDTO(Event event) {
        UserDTO creatorDto = null;
        if (event.getCreator() != null) {
            creatorDto = new UserDTO(
                    event.getCreator().getId(),
                    event.getCreator().getUsername(),
                    event.getCreator().getEmail(),
                    event.getCreator().getRole().name(),
                    event.getCreator().getName(),
                    event.getCreator().getAge(),
                    event.getCreator().getCreatedAt()
            );
        }

        EventDTO dto = EventDTO.builder()
                .id(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .venue(event.getVenue())
                .location(event.getLocation())
                .price(event.getPrice())
                .totalSeats(event.getTotalSeats())
                .availableSeats(event.getAvailableSeats())
                .creator(creatorDto)
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .build();
        return dto;
    }

    public void delete(UUID id, User currentUser) throws AccessDeniedException {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException("Event not found"));

        boolean isAdmin = "ADMIN".equals(currentUser.getRole().toString());
        boolean isOwner = event.getCreator().getId().equals(currentUser.getId());
        boolean isActive = event.getAvailableSeats() > 0;

        if (!isAdmin && (!isOwner || !isActive)) {
            throw new AccessDeniedException("You cannot delete this event.");
        }

        eventRepository.deleteById(id);
    }

    public List<Event> findEventsByCreator(User user) {
        return eventRepository.findAllByCreator(user);
    }

    @Cacheable("upcomingEvents")
    public void getUpcomingEvents() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime thirtyDaysLater = now.plusDays(30);
        eventRepository.findByStartDateBetween(now, thirtyDaysLater);
    }

    @Transactional
    @CacheEvict(value = "upcomingEvents", allEntries = true)
    public void removeExpiredEvents() {
        eventRepository.deleteAllByEndDateBefore(LocalDateTime.now());
    }

    public EventAnalyticsDTO mapToAnalytics(Event event) {
        return new EventAnalyticsDTO(
                event.getId(),
                event.getName(),
                event.getTotalSeats(),
                event.getPrice()
        );
    }

}
