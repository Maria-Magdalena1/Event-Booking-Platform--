package main.service;

import lombok.RequiredArgsConstructor;
import main.entity.Booking;
import main.repositories.BookingRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {
    private final BookingRepository bookingRepository;

    public void save(Booking booking) {
        bookingRepository.save(booking);
    }

    public List<Booking> findAll() {
        return bookingRepository.findAll();
    }

    public Object count() {
        return bookingRepository.count();
    }
}
