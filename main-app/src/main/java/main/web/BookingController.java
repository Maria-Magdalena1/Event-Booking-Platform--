package main.web;

import main.entities.Booking;
import main.entities.Event;
import main.entities.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import main.exceptions.BookingAlreadyCancelledException;
import main.exceptions.BookingAlreadyConfirmedException;
import main.exceptions.NotFoundBookingException;
import main.microservices.AnalyticsClient;
import main.security.UserData;
import main.services.*;
import main.web.dto.BookingAnalyticsDTO;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import main.web.dto.CreateBookingDTO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final EventService eventService;
    private final UserService userService;
    private final PdfService pdfService;
    private final BookingReminderService bookingReminderService;
    private final AnalyticsClient analyticsClient;

    @GetMapping("/create/{eventId}")
    public ModelAndView showBookingForm(@PathVariable UUID eventId,
                                        @RequestParam(required = false) String from,
                                        Principal principal) {
        Event event = eventService.findById(eventId);
        if (event == null) {
            return new ModelAndView("redirect:/events");
        }

        UUID creatorId = event.getCreator() != null ? event.getCreator().getId() : null;

        ModelAndView mav = new ModelAndView("bookings/booking-form");
        mav.addObject("event", event);
        mav.addObject("creatorId", creatorId);
        mav.addObject("booking", new CreateBookingDTO());
        mav.addObject("backUrl", from != null ? from : "/events");
        mav.addObject("currentUserId", principal != null ? userService.findByUsername(principal.getName()).getId() : null);
        return mav;
    }

    @PutMapping("/create/{eventId}")
    public ModelAndView createBooking(@PathVariable UUID eventId,
                                      @ModelAttribute("booking") @Valid CreateBookingDTO bookingDTO,
                                      BindingResult bindingResult,
                                      @AuthenticationPrincipal UserData userData) {

        Event event = eventService.findById(eventId);
        ModelAndView mav = new ModelAndView("bookings/booking-form");

        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors().forEach(System.out::println);
            mav.addObject("event", event);
            return mav;
        }

        if (bookingDTO.getSeatsBooked() <= 0 || bookingDTO.getSeatsBooked() > event.getAvailableSeats()) {
            mav.addObject("seatError", "Invalid number of seats selected.");
            return mav;
        }

        User user = userService.findByUsername(userData.getUsername());
        Booking booking = bookingService.createBooking(user, event,
                bookingDTO.getSeatsBooked());

        mav.setViewName("bookings/booking-confirmation");
        mav.addObject("booking", booking);
        return mav;
    }

    @GetMapping("/confirm/{bookingId}")
    public ModelAndView showConfirmation(@PathVariable UUID bookingId) {
        Booking booking = bookingService.getBooking(bookingId);
        if (booking == null) {
            return new ModelAndView("redirect:/events");
        }
        ModelAndView mav = new ModelAndView("bookings/booking-confirmation");
        mav.addObject("booking", booking);
        mav.addObject("event", booking.getEvent());
        return mav;
    }

    @PostMapping("/confirm/{bookingId}")
    public ModelAndView confirmBooking(@PathVariable UUID bookingId) {
        ModelAndView mav = new ModelAndView("bookings/booking-status");

        try {
            Booking booking = bookingService.confirmBooking(bookingId);
            File pdf = pdfService.generateBookingPdf(
                    booking.getUser().getName(),
                    booking.getUser().getEmail(),
                    booking.getEvent().getName(),
                    booking.getEvent().getStartDate(),
                    booking.getEvent().getEndDate(),
                    booking.getSeatsBooked(),
                    LocalDateTime.now(),
                    booking.getTotalPrice()
            );

            String googleCalendarLink = bookingReminderService.getGoogleCalendarLink(
                    booking.getEvent().getName(),
                    "Booking for " + booking.getUser().getName() + " (" + booking.getUser().getEmail() + ") "
                            + booking.getEvent().getDescription(),
                    booking.getEvent().getStartDate(),
                    booking.getEvent().getEndDate()
            );
            //String startISO = booking.getEvent().getStartDate().toString();
            //String endISO = booking.getEvent().getEndDate().toString();
            //bookingReminderService.createBookingReminder(
            //        booking.getEvent().getName(),
            //        "Booking for " + booking.getUser().getName(),
            //        startISO,
            //        endISO,
            //        30,
            //        60
            //);

            BookingAnalyticsDTO bookingDTO = new BookingAnalyticsDTO();
            bookingDTO.setId(booking.getId());
            bookingDTO.setEventId(booking.getEvent().getId());
            bookingDTO.setUserId(booking.getUser().getId());
            bookingDTO.setSeatsBooked(booking.getSeatsBooked());
            bookingDTO.setPrice(booking.getTotalPrice());

            analyticsClient.confirmBooking(bookingDTO);

            mav.addObject("message", "Your booking has been confirmed successfully!");
            mav.addObject("booking", booking);
            mav.addObject("event", booking.getEvent());

            mav.addObject("pdfFileName", pdf.getName());
            mav.addObject("googleCalendarLink", googleCalendarLink);

        } catch (NotFoundBookingException e) {
            mav.addObject("message", "Booking not found.");
        } catch (BookingAlreadyConfirmedException e) {
            mav.addObject("message", "This booking is already confirmed.");
        } catch (IllegalStateException e) {
            mav.addObject("message", e.getMessage());
        } catch (Exception e) {
            mav.addObject("message", "An unexpected error occurred while confirming the booking");
        }

        return mav;
    }

    @GetMapping("/cancel/{bookingId}")
    public ModelAndView showCancelPage(@PathVariable UUID bookingId) {
        ModelAndView mav = new ModelAndView("bookings/booking-confirmation");

        try {
            Booking booking = bookingService.cancelBooking(bookingId);
            mav.addObject("booking", booking);
            mav.addObject("event", booking.getEvent());
            mav.addObject("message", "Your booking has been cancelled.");
        } catch (Exception e) {
            mav.addObject("message", e.getMessage());
        }

        return mav;
    }

    @PostMapping("/cancel/{id}")
    public ModelAndView cancelBooking(@PathVariable UUID id) {
        ModelAndView mav = new ModelAndView("bookings/booking-status");

        try {
            Booking booking = bookingService.cancelBooking(id);
            Event event = booking.getEvent();

            mav.addObject("message", "Your booking has been cancelled successfully.");
            mav.addObject("booking", booking);
            mav.addObject("event", event);
        } catch (NotFoundBookingException e) {
            mav.addObject("message", "Booking not found.");
        } catch (BookingAlreadyCancelledException e) {
            mav.addObject("message", "This booking has already been cancelled.");
        } catch (Exception e) {
            mav.addObject("message", "An unexpected error occurred while cancelling the booking.");
        }
        return mav;
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> downloadPdf(@PathVariable String fileName) throws IOException {
        File file = new File(fileName);
        byte[] pdfBytes = Files.readAllBytes(file.toPath());

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + file.getName())
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

}
