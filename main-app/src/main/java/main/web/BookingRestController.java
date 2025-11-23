package main.web;

import lombok.RequiredArgsConstructor;
import main.entities.Booking;
import main.entities.Event;
import main.entities.User;
import main.services.BookingService;
import main.services.EventService;
import main.services.UserService;
import main.web.dto.BookingAnalyticsDTO;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.UUID;

@RestController
@RequestMapping("/api/user/bookings")
@RequiredArgsConstructor
public class BookingRestController {
    private final BookingService bookingService;
    private final UserService userService;
    private final EventService eventService;

    @PostMapping
    public Booking createBooking(@RequestBody BookingAnalyticsDTO dto) {
        User user = userService.findById(dto.getUserId());
        Event event = eventService.findById(dto.getEventId());
        return bookingService.createBooking(user, event, dto.getSeatsBooked());
    }

    @PostMapping("/confirm/{id}")
    public Booking confirmBooking(@PathVariable UUID id) {
        return bookingService.confirmBooking(id);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadQRCode(@PathVariable UUID id) {
        Booking booking = bookingService.getBooking(id);
        byte[] qrImage = Base64.getDecoder().decode(booking.getQrCodeBase64());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"booking.png\"")
                .body(qrImage);
    }
}
