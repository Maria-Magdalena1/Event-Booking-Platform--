package main.services;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import main.entities.Booking;
import main.entities.BookingStatus;
import main.entities.Event;
import main.entities.User;
import main.exceptions.BookingAlreadyCancelledException;
import main.exceptions.BookingAlreadyConfirmedException;
import main.exceptions.NoAvailableSeatsException;
import main.exceptions.NotFoundBookingException;
import lombok.RequiredArgsConstructor;
import main.web.dto.BookingAnalyticsDTO;
import org.springframework.stereotype.Service;
import main.repositories.BookingRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final EventService eventService;
    private final QRCodeService qrCodeService;

    @Transactional
    public Booking createBooking(User user, Event event, int seats) {
        log.info("User {} is attempting to create a booking for event {} with {} seats",
                user.getUsername(), event.getName(), seats);
        if (event.getAvailableSeats() < seats) {
            log.warn("Booking failed: Not enough seats available for event {}", event.getName());
            throw new NoAvailableSeatsException("Not enough available seats");
        }

        Booking booking = Booking.builder()
                .user(user)
                .event(event)
                .seatsBooked(seats)
                .bookedOn(LocalDateTime.now())
                .status(BookingStatus.PENDING)
                .build();

        bookingRepository.save(booking);
        eventService.save(event);

        log.info("Booking created successfully with id {}", booking.getId());
        return booking;
    }

    @Transactional
    public Booking confirmBooking(UUID bookingId) {
        log.info("Attempting to confirm booking with id {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Booking {} not found!", bookingId);
                    return new NotFoundBookingException("Booking not found");
                });

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            log.warn("Booking {} is already confirmed", bookingId);
            throw new BookingAlreadyConfirmedException("Booking already confirmed");
        }

        Event event = booking.getEvent();

        if (booking.getSeatsBooked() > event.getAvailableSeats()) {
            log.error("Cannot confirm booking {}: not enough seats", bookingId);
            throw new IllegalStateException("Not enough available seats to confirm this booking.");
        }

        event.setAvailableSeats(event.getAvailableSeats() - booking.getSeatsBooked());
        eventService.update(event);

        double totalPrice = event.getPrice() * booking.getSeatsBooked();
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.CONFIRMED);

        String qrData = "Event: " + booking.getEvent().getName()
                + "Seats: " + booking.getSeatsBooked()
                + "Start Date: " + booking.getEvent().getStartDate()
                + "End Date: " + booking.getEvent().getEndDate();

        String qrBase64 = qrCodeService.generateQRCodeBase64(qrData);
        booking.setQrCodeBase64(qrBase64);

        bookingRepository.save(booking);

        log.info("Booking {} confirmed successfully for user {}", bookingId, booking.getUser().getUsername());
        return booking;
    }

    @Transactional
    public Booking cancelBooking(UUID bookingId) {
        log.info("Attempting to cancel booking with id {}", bookingId);

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> {
                    log.warn("Booking {} not found", bookingId);
                    return new NotFoundBookingException("Booking not found");
                });

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            log.warn("Booking {} is already cancelled", bookingId);
            throw new BookingAlreadyCancelledException("Booking already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        save(booking);

        log.info("Booking {} cancelled successfully", bookingId);
        return booking;
    }

    public List<Booking> findByUser(User user) {
        return bookingRepository.findByUser(user);
    }

    public void save(Booking booking) {
        bookingRepository.save(booking);
    }

    public Booking getBooking(UUID bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundBookingException("Booking not found"));
    }

    public List<BookingAnalyticsDTO> getAllBookings() {
        return bookingRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private BookingAnalyticsDTO mapToDTO(Booking booking) {
        BookingAnalyticsDTO dto = new BookingAnalyticsDTO();
        dto.setId(booking.getId());
        dto.setEventId(booking.getEvent().getId());
        dto.setUserId(booking.getUser().getId());
        dto.setSeatsBooked(booking.getSeatsBooked());
        dto.setPrice(booking.getTotalPrice());
        return dto;
    }

}
