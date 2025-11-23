package main.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingDTO {
    private UUID id;
    private UUID eventId;
    private UUID userId;
    private int seatsBooked;
    private double price;
}
