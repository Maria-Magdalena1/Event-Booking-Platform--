package main.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Min(1)
    @Column(nullable = false)
    private int seatsBooked;

    @Column(nullable = false, updatable = false)
    private LocalDateTime bookedOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull
    private BookingStatus status;

    @Column(nullable = false)
    private double totalPrice;

    @Lob
    private String qrCodeBase64;
}
