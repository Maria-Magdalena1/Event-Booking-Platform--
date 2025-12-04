package main.microservices;

import main.web.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@FeignClient(name = "analytics-microservice", url = "http://localhost:8081/api")
public interface AnalyticsClient {

    @GetMapping("/dashboard")
    Map<String, Object> getDashboard();

    @PostMapping("/events")
    void addEvent(@RequestBody EventAnalyticsDTO event);

    @PostMapping("/bookings")
    void confirmBooking(@RequestBody BookingAnalyticsDTO booking);

    @PostMapping("/users")
    void addUser(@RequestBody UserAnalyticsDTO user);

}
