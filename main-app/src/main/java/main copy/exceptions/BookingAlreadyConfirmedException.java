package main.exceptions;

public class BookingAlreadyConfirmedException extends RuntimeException {
    public BookingAlreadyConfirmedException(String message) {
        super(message);
    }
}
