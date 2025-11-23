package main.web.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDTO {

    private UUID id;

    @NotBlank
    private String name;

    @NotBlank
    @Size(min = 20, max = 500)
    private String description;

    @NotNull
    @Future(message = "Start date must be in the future")
    private LocalDateTime startDate;

    @NotNull
    @Future(message = "End date must be in the future")
    private LocalDateTime endDate;

    @NotBlank
    private String venue;

    @NotBlank
    private String location;

    @NotNull
    @PositiveOrZero
    private double price;

    @Positive
    @NotNull
    private int totalSeats;

    private Integer availableSeats;

    private UserDTO creator;

    private boolean canBook;

    private boolean canEditDelete;

}
