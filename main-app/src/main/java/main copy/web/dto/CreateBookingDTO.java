package main.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateBookingDTO {

    private UUID eventId;

    @NotNull
    @Min(value = 1)
    private int seatsBooked;

}
