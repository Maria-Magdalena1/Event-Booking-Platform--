package main.security;

import lombok.RequiredArgsConstructor;
import main.entities.Event;
import main.services.EventService;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("eventSecurity")
@RequiredArgsConstructor
public class EventSecurity {
    private final EventService eventService;

    public boolean isOwner(UUID eventId, UUID userId) {
        Event event = eventService.findById(eventId);
        if (event.getCreator() == null) {
            return false;
        }
        return event.getCreator().getId().equals(userId);
    }

    public boolean canEditOrDelete(UUID eventId, UUID userId, String role) {
        Event event = eventService.findById(eventId);
        if (event == null) return false;

        boolean isAdmin = "ADMIN".equals(role);
        boolean isOwner = isOwner(eventId, userId);
        boolean isActive = event.getAvailableSeats() > 0;

        return isAdmin || (isOwner && isActive);
    }
}
