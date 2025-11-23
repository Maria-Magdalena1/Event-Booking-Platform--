package main.entities;

import lombok.Getter;

@Getter
public enum BookingStatus {
    PENDING("Waiting for confirmation"),
    CONFIRMED("Confirmed"),
    CANCELLED("Cancelled");

    private final String displayName;

    BookingStatus(String displayName) {
        this.displayName = displayName;
    }

}
