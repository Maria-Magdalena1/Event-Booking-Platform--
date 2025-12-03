package main.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Invalid argument");
        body.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(InvalidEventDataException.class)
    public ResponseEntity<?> handleInvalidEventData(InvalidEventDataException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Invalid event data");
        body.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }
}
