package main.web.dto;

import main.entities.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ConfirmBookingDTO {

    @NotNull
    private UUID bookingId;

    @NotNull
    private BookingStatus status;
}
