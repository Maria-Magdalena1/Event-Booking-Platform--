package main.web;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import main.entities.Event;
import jakarta.validation.Valid;
import main.entities.Role;
import main.entities.User;
import main.exceptions.EventNotFoundException;
import main.microservices.AnalyticsClient;
import main.services.AiService;
import main.services.UserService;
import main.web.dto.EditEventDTO;
import main.web.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import main.services.EventService;
import main.web.dto.EventDTO;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.nio.file.AccessDeniedException;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private final UserService userService;
    private final AiService aiService;
    private final AnalyticsClient analyticsClient;

    @Autowired
    public EventController(EventService eventService, UserService userService, AiService aiService, AnalyticsClient analyticsClient) {
        this.eventService = eventService;
        this.userService = userService;
        this.aiService = aiService;
        this.analyticsClient = analyticsClient;
    }


    @GetMapping
    public ModelAndView eventPage(Principal principal,
                                  @ModelAttribute("successMessage") String successMessage,
                                  @ModelAttribute("errorMessage") String errorMessage) {
        if (principal != null) {
            log.info("User {} accessed the events page", principal.getName());
        } else {
            log.info("Anonymous user accessed the events page");
        }

        ModelAndView mav = new ModelAndView("events/events");

        Optional<User> currentUserOpt = Optional.ofNullable(principal)
                .map(p -> userService.findByUsername(p.getName()))
                .map(u -> userService.findById(u.getId()));

        List<EventDTO> events = eventService.findUpcomingEvents()
                .stream()
                .map(event -> {
                    EventDTO eventDTO = eventService.mapToDTO(event);

                    currentUserOpt.ifPresent(currentUser -> {
                        UUID creatorId = eventDTO.getCreator() != null ? eventDTO.getCreator().getId() : null;
                        boolean canBook = eventDTO.getAvailableSeats() != null
                                && eventDTO.getAvailableSeats() > 0
                                && (creatorId == null || !creatorId.equals(currentUser.getId()));
                        eventDTO.setCanBook(canBook);

                        boolean canEditDelete = currentUser.getRole() == Role.ADMIN ||
                                ((creatorId != null && creatorId.equals(currentUser.getId())) && eventDTO.getAvailableSeats() > 0);

                        eventDTO.setCanEditDelete(canEditDelete);
                    });
                    currentUserOpt.ifPresent(user -> mav.addObject("isAdmin", user.getRole() == Role.ADMIN));

                    return eventDTO;
                })
                .collect(Collectors.toList());
        mav.addObject("events", events);

        currentUserOpt.ifPresent(user -> mav.addObject("currentUser", user));

        if (successMessage != null && !successMessage.isEmpty()) {
            mav.addObject("successMessage", successMessage);
        }

        if (errorMessage != null && !errorMessage.isEmpty()) {
            mav.addObject("errorMessage", errorMessage);
        }

        return mav;
    }

    @GetMapping("/add-event")
    public ModelAndView showAddEventForm() {
        ModelAndView mav = new ModelAndView("events/add-event");
        mav.addObject("event", new EventDTO());
        return mav;
    }

    @PostMapping("/add-event")
    public ModelAndView addEvent(@ModelAttribute("event") @Valid EventDTO eventDTO,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Principal principal,
                                 @RequestParam(required = false) String action,
                                 @RequestParam(required = false) String aiTitle) {

        if (eventDTO.getStartDate() != null && eventDTO.getEndDate() != null &&
                eventDTO.getEndDate().isBefore(eventDTO.getStartDate())) {
            bindingResult.rejectValue("startDate", "invalidDates", "start date must be before end date");
        }

        if ("generateDescription".equals(action)) {
            log.info("User {} requested AI description for '{}'", principal.getName(), aiTitle);
            if (aiTitle != null && !aiTitle.isBlank()) {
                String generatedDescription = aiService.generateEventDescription(aiTitle);
                eventDTO.setDescription(generatedDescription);
            }

            ModelAndView mav = new ModelAndView("events/add-event");
            mav.addObject("event", eventDTO);
            mav.addObject("aiTitle", aiTitle);
            mav.addObject("aiDescription", eventDTO.getDescription());

            return mav;
        }

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while adding event by user {}: {}", principal.getName(), bindingResult.getAllErrors());
            return new ModelAndView("events/add-event");
        }

        try {
            User user = userService.findByUsername(principal.getName());

            Event event = eventService.create(eventDTO, user);
            analyticsClient.addEvent(eventService.mapToAnalytics(event));
            log.info("User {} created event '{}' with id {}", principal.getName(), event.getName(), event.getId());
            redirectAttributes.addFlashAttribute("successMessage", "Event created successfully!");
        } catch (IllegalArgumentException e) {
            log.warn("User {} failed to create event: {}", principal.getName(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return new ModelAndView("redirect:/events/add-event");
        } catch (Exception e) {
            log.error("Unexpected error while user {} tried to create event: {}", principal.getName(), e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create event!");
            return new ModelAndView("redirect:/events/add-event");
        }
        return new ModelAndView("redirect:/events");
    }

    @GetMapping("/{id}")
    public ModelAndView eventDetails(@PathVariable UUID id, Principal principal) {
        log.info("Event details requested for id {}", id);
        Event event = eventService.findById(id);
        ModelAndView mav = new ModelAndView("events/event-details");

        EventDTO eventDTO = eventService.mapToDTO(event);

        if (principal != null) {
            User currentUser = userService.findByUsername(principal.getName());

            UUID creatorId = Optional.ofNullable(eventDTO.getCreator())
                    .map(UserDTO::getId)
                    .orElse(null);

            boolean canBook = creatorId == null || !creatorId.equals(currentUser.getId());
            canBook = canBook && eventDTO.getAvailableSeats() != null && eventDTO.getAvailableSeats() > 0;
            eventDTO.setCanBook(canBook);
        }
        mav.addObject("event", eventDTO);
        return mav;
    }

    @PreAuthorize("@eventSecurity.canEditOrDelete(#id, authentication.principal.userId, authentication.principal.role)")
    @GetMapping("/{id}/event-edit")
    public ModelAndView showEditForm(@PathVariable UUID id) {
        log.info("Edit form requested for event {}", id);
        Event existingEvent = eventService.findById(id);
        if (existingEvent == null) {
            throw new EventNotFoundException("Event not found");
        }

        EventDTO eventDTO = EventDTO.builder()
                .id(existingEvent.getId())
                .name(existingEvent.getName())
                .description(existingEvent.getDescription())
                .venue(existingEvent.getVenue())
                .location(existingEvent.getLocation())
                .price(existingEvent.getPrice())
                .startDate(existingEvent.getStartDate() != null
                        ? existingEvent.getStartDate().withSecond(0).withNano(0)
                        : null)
                .endDate(existingEvent.getEndDate() != null
                        ? existingEvent.getEndDate().withSecond(0).withNano(0)
                        : null)
                .build();
        ModelAndView mav = new ModelAndView("events/event-edit");
        mav.addObject("event", eventDTO);

        return mav;
    }

    @PreAuthorize("@eventSecurity.canEditOrDelete(#id, authentication.principal.userId, authentication.principal.role)")
    @PutMapping("/{id}/event-edit")
    public ModelAndView editEvent(@PathVariable UUID id,
                                  @ModelAttribute("event") @Valid EditEventDTO eventDTO,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {
        log.info("User submitted edit for event {}", id);

        if (eventDTO.getStartDate() != null && eventDTO.getEndDate() != null &&
                eventDTO.getEndDate().isBefore(eventDTO.getStartDate())) {

            bindingResult.rejectValue(
                    "startDate", "invalidDates", "start date must be before end date");
        }

        if (bindingResult.hasErrors()) {
            log.warn("Validation errors while editing event {}: {}", id, bindingResult.getAllErrors());
            ModelAndView mav = new ModelAndView("events/event-edit");
            mav.addObject("event", eventDTO);
            return mav;
        }
        eventService.update(id, eventDTO);
        log.info("Event {} updated successfully", id);

        redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
        return new ModelAndView("redirect:/events");
    }

    @Transactional
    @PreAuthorize("@eventSecurity.canEditOrDelete(#id, authentication.principal.userId, authentication.principal.role)")
    @PostMapping("/{id}/delete")
    public String deleteEvent(@PathVariable UUID id, Principal principal) throws AccessDeniedException {
        log.info("User {} is attempting to delete event {}", principal.getName(), id);

        User currentUser = userService.findByUsername(principal.getName());
        eventService.delete(id, currentUser);

        log.info("Event {} deleted successfully by user {}", id, principal.getName());
        return "redirect:/events";
    }

}
