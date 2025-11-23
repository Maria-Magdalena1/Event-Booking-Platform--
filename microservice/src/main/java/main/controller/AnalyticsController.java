package main.controller;

import lombok.RequiredArgsConstructor;
import main.entity.Booking;
import main.entity.Event;
import main.entity.User;
import main.service.BookingService;
import main.service.EventService;
import main.service.UserService;
import main.web.dto.BookingDTO;
import main.web.dto.EventDTO;
import main.web.dto.UserDTO;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalyticsController {

    private final UserService userService;
    private final EventService eventService;
    private final BookingService bookingService;

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("totalUsers", userService.count());
        dashboard.put("totalEvents", eventService.count());
        dashboard.put("totalBookings", bookingService.count());

        double totalRevenue = bookingService.findAll().stream()
                .mapToDouble(Booking::getPrice).sum();
        dashboard.put("totalRevenue", totalRevenue);

        List<Map<String, Object>> topEvents = eventService.findAll().stream()
                .map(e -> {
                    int bookedSeats = bookingService.findAll().stream()
                            .filter(b -> b.getEventId().equals(e.getId()))
                            .mapToInt(Booking::getSeatsBooked)
                            .sum();
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getName());
                    m.put("totalSeatsBooked", bookedSeats);
                    return m;
                })
                .sorted((a, b) -> ((Integer) b.get("totalSeatsBooked")).compareTo((Integer) a.get("totalSeatsBooked")))
                .limit(3)
                .toList();

        dashboard.put("topEvents", topEvents);

        Map<UUID, Integer> bookingsByUser = bookingService.findAll().stream()
                .collect(Collectors.groupingBy(
                        Booking::getUserId,
                        Collectors.summingInt(Booking::getSeatsBooked)
                ));

        List<Map<String, Object>> topUsers = userService.findAll().stream()
                .map(u -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("username", u.getUsername());
                    m.put("totalBookedSeats", bookingsByUser.getOrDefault(u.getId(), 0));
                    return m;
                })
                .sorted((a, b) -> ((Integer) b.get("totalBookedSeats")).compareTo((Integer) a.get("totalBookedSeats")))
                .limit(5)
                .toList();

        dashboard.put("topUsers", topUsers);

        Map<UUID, Integer> seatsByEvent = bookingService.findAll().stream()
                .collect(Collectors.groupingBy(
                        Booking::getEventId,
                        Collectors.summingInt(Booking::getSeatsBooked)
                ));

        List<Map<String, Object>> seatWarnings = eventService.findAll().stream()
                .map(e -> {
                    int booked = seatsByEvent.getOrDefault(e.getId(), 0);
                    int freeSeats = e.getTotalSeats() - booked;
                    Map<String, Object> m = new HashMap<>();
                    m.put("eventName", e.getName());
                    m.put("freeSeats", freeSeats);
                    return m;
                })
                .filter(m -> (Integer) m.get("freeSeats") <= 5)
                .toList();

        dashboard.put("seatWarnings", seatWarnings);
        return dashboard;
    }

    @GetMapping("/events")
    public List<EventDTO> getAllEvents() {
        return eventService.findAll().stream()
                .map(event -> new EventDTO(
                        event.getId(),
                        event.getName(),
                        event.getTotalSeats(),
                        event.getPrice()
                ))
                .collect(Collectors.toList());
    }

    @PostMapping("/events")
    public void addEvent(@RequestBody EventDTO dto) {
        Event event = new Event(dto.getId(), dto.getName(), dto.getTotalSeats(), dto.getPrice());
        eventService.save(event);
    }

    @GetMapping("/bookings")
    public List<BookingDTO> getAllBookings() {
        return bookingService.findAll().stream()
                .map(b -> new BookingDTO(
                        b.getId(),
                        b.getEventId(),
                        b.getUserId(),
                        b.getSeatsBooked(),
                        b.getPrice()
                ))
                .collect(Collectors.toList());
    }

    @PostMapping("/bookings")
    public void confirmBooking(@RequestBody BookingDTO dto) {
        Booking booking = new Booking(dto.getId(), dto.getEventId(), dto.getUserId(), dto.getSeatsBooked(), dto.getPrice());
        bookingService.save(booking);
    }

    @GetMapping("/users")
    public List<UserDTO> getAllUsers() {
        return userService.findAll().stream()
                .map(b -> new UserDTO(
                        b.getId(),
                        b.getUsername()
                ))
                .collect(Collectors.toList());
    }

    @PostMapping("/users")
    public void addUser(@RequestBody UserDTO dto) {
        User user = new User(dto.getId(), dto.getUsername());
        userService.save(user);
    }

}
