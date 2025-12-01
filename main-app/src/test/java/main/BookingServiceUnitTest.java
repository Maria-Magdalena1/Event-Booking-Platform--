package main;

import main.entities.*;
import main.exceptions.BookingAlreadyCancelledException;
import main.exceptions.BookingAlreadyConfirmedException;
import main.exceptions.NoAvailableSeatsException;
import main.exceptions.NotFoundBookingException;
import main.repositories.BookingRepository;
import main.services.BookingService;
import main.services.EventService;
import main.services.QRCodeService;
import main.web.dto.BookingAnalyticsDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceUnitTest {

    @InjectMocks
    private BookingService bookingService;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventService eventService;

    @Mock
    private QRCodeService qrCodeService;

    private User user;
    private Event event;
    private Booking booking;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setRole(Role.USER);

        event = new Event();
        event.setId(UUID.randomUUID());
        event.setName("Concert");
        event.setPrice(50.0);
        event.setAvailableSeats(100);
        event.setTotalSeats(100);

        booking = Booking.builder()
                .id(UUID.randomUUID())
                .user(user)
                .event(event)
                .seatsBooked(2)
                .status(BookingStatus.PENDING)
                .build();
    }

    @Test
    void testCreateBooking_Success() {
        event.setAvailableSeats(5);

        Mockito.when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenAnswer(i -> i.getArgument(0));

        Booking created = bookingService.createBooking(user, event, 2);

        assertEquals(user, created.getUser());
        assertEquals(event, created.getEvent());
        assertEquals(BookingStatus.PENDING, created.getStatus());
    }

    @Test
    void testCreateBooking_NotEnoughSeats() {
        event.setAvailableSeats(1);

        assertThrows(NoAvailableSeatsException.class,
                () -> bookingService.createBooking(user, event, 2));
    }

    @Test
    void testConfirmBooking_Success() {
        booking.setStatus(BookingStatus.PENDING);
        event.setAvailableSeats(100);

        Mockito.when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));
        Mockito.when(qrCodeService.generateQRCodeBase64(Mockito.anyString()))
                .thenReturn("QRCodeBase64");

        Booking confirmed = bookingService.confirmBooking(booking.getId());

        assertEquals(BookingStatus.CONFIRMED, confirmed.getStatus());
        assertEquals("QRCodeBase64", confirmed.getQrCodeBase64());
        assertEquals(event.getPrice() * booking.getSeatsBooked(), confirmed.getTotalPrice());

        int expectedAvailableSeats = 98;
        assertEquals(expectedAvailableSeats, event.getAvailableSeats());

        Mockito.verify(eventService).update(event);
    }

    @Test
    void testConfirmBooking_AlreadyConfirmed() {
        booking.setStatus(BookingStatus.CONFIRMED);

        Mockito.when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        assertThrows(BookingAlreadyConfirmedException.class,
                () -> bookingService.confirmBooking(booking.getId()));
    }

    @Test
    void testConfirmBooking_NotEnoughSeatsInEvent() {
        booking.setStatus(BookingStatus.PENDING);
        event.setAvailableSeats(1);
        booking.setSeatsBooked(5);

        Mockito.when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        assertThrows(IllegalStateException.class,
                () -> bookingService.confirmBooking(booking.getId()));
    }

    @Test
    void testConfirmBooking_NotFound() {
        UUID id = UUID.randomUUID();
        Mockito.when(bookingRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundBookingException.class,
                () -> bookingService.confirmBooking(id));
    }

    @Test
    void testCancelBooking_Success() {
        booking.setStatus(BookingStatus.PENDING);

        Mockito.when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        Booking cancelled = bookingService.cancelBooking(booking.getId());

        assertEquals(BookingStatus.CANCELLED, cancelled.getStatus());
    }

    @Test
    void testCancelBooking_AlreadyCancelled() {
        booking.setStatus(BookingStatus.CANCELLED);

        Mockito.when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        assertThrows(BookingAlreadyCancelledException.class,
                () -> bookingService.cancelBooking(booking.getId()));
    }

    @Test
    void testCancelBooking_NotFound() {
        UUID id = UUID.randomUUID();
        Mockito.when(bookingRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundBookingException.class,
                () -> bookingService.cancelBooking(id));
    }

    @Test
    void testFindByUser() {
        Mockito.when(bookingRepository.findByUser(user))
                .thenReturn(Collections.singletonList(booking));

        List<Booking> list = bookingService.findByUser(user);

        assertFalse(list.isEmpty());
        assertEquals(user, list.get(0).getUser());
    }

    @Test
    void testSave() {
        Mockito.when(bookingRepository.save(Mockito.any(Booking.class)))
                .thenAnswer(i -> i.getArgument(0));

        bookingService.save(booking);

        Mockito.verify(bookingRepository).save(booking);
    }

    @Test
    void testGetBooking_Success() {
        Mockito.when(bookingRepository.findById(booking.getId()))
                .thenReturn(Optional.of(booking));

        Booking b = bookingService.getBooking(booking.getId());
        assertEquals(booking.getId(), b.getId());
    }

    @Test
    void testGetBooking_NotFound() {
        UUID id = UUID.randomUUID();
        Mockito.when(bookingRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundBookingException.class,
                () -> bookingService.getBooking(id));
    }

    @Test
    void testGetAllBookings() {
        Mockito.when(bookingRepository.findAll())
                .thenReturn(Collections.singletonList(booking));

        List<BookingAnalyticsDTO> dtos = bookingService.getAllBookings();

        assertFalse(dtos.isEmpty());
        assertEquals(booking.getId(), dtos.get(0).getId());
        assertEquals(booking.getSeatsBooked(), dtos.get(0).getSeatsBooked());
        assertEquals(booking.getTotalPrice(), dtos.get(0).getPrice());
    }
}
