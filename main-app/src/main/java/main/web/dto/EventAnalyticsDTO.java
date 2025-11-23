package main.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventAnalyticsDTO {
    private UUID id;
    private String name;
    private int totalSeats;
    private double price;
}
