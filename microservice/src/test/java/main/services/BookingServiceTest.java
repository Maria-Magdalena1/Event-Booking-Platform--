package main.service;

import main.entity.Booking;
import main.repositories.BookingRepository;
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
public class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void save_callsRepositorySave() {
        Booking booking = new Booking();

        bookingService.save(booking);

        verify(bookingRepository, times(1)).save(booking);
    }

    @Test
    void findAll_returnsListFromRepository() {
        Booking booking1 = new Booking();
        Booking booking2 = new Booking();
        List<Booking> bookings = Arrays.asList(booking1, booking2);

        when(bookingRepository.findAll()).thenReturn(bookings);

        List<Booking> result = bookingService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(bookings, result);

        verify(bookingRepository, times(1)).findAll();
    }

    @Test
    void count_returnsRepositoryCount() {
        long expectedCount = 5L;
        when(bookingRepository.count()).thenReturn(expectedCount);

        Object result = bookingService.count();

        assertEquals(expectedCount, result);
        verify(bookingRepository, times(1)).count();
    }
}
