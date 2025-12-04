package main.integration;

import main.entity.Booking;
import main.service.BookingService;
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
public class BookingServiceIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Test
    void save_booking_isPersisted() {
        Booking booking = Booking.builder()
                .id(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .seatsBooked(2)
                .price(100.0)
                .build();

        bookingService.save(booking);

        List<Booking> bookings = bookingService.findAll();
        assertEquals(1, bookings.size());
        assertEquals(2, bookings.get(0).getSeatsBooked());
        assertEquals(100.0, bookings.get(0).getPrice());
    }

    @Test
    void findAll_returnsAllBookings() {
        Booking booking1 = Booking.builder()
                .id(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .seatsBooked(2)
                .price(100.0)
                .build();

        Booking booking2 = Booking.builder()
                .id(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .seatsBooked(4)
                .price(200.0)
                .build();

        bookingService.save(booking1);
        bookingService.save(booking2);

        List<Booking> bookings = bookingService.findAll();
        assertEquals(2, bookings.size());
        assertTrue(bookings.stream().anyMatch(b -> b.getSeatsBooked() == 2 && b.getPrice() == 100.0));
        assertTrue(bookings.stream().anyMatch(b -> b.getSeatsBooked() == 4 && b.getPrice() == 200.0));
    }

    @Test
    void findAll_emptyList() {
        List<Booking> bookings = bookingService.findAll();
        assertTrue(bookings.isEmpty());
    }

    @Test
    void count_returnsNumberOfBookings() {
        Booking booking1 = Booking.builder()
                .id(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .seatsBooked(2)
                .price(100.0)
                .build();

        Booking booking2 = Booking.builder()
                .id(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .seatsBooked(4)
                .price(200.0)
                .build();

        bookingService.save(booking1);
        bookingService.save(booking2);

        Object count = bookingService.count();
        assertEquals(2L, count);
    }

    @Test
    void count_emptyDatabase() {
        Object count = bookingService.count();
        assertEquals(0L, count);
    }
}
