package main.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Event {
    @Id
    private UUID id;
    private String name;
    private int totalSeats;
    private double price;
}
