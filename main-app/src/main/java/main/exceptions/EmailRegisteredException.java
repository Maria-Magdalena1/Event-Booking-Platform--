package main.exceptions;

public class EmailRegisteredException extends RuntimeException {
    public EmailRegisteredException(String message) {
        super(message);
    }
}
