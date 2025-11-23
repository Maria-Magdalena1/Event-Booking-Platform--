package main.web;

import lombok.RequiredArgsConstructor;
import main.services.AdminDashboardService;

import main.web.dto.*;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AnalyticsRestController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard() {
        return adminDashboardService.getDashboard();
    }

    @PostMapping("/events")
    public void addEvent(@RequestBody EventAnalyticsDTO event) {
        adminDashboardService.addEvent(event);
    }

    @PostMapping("/bookings")
    public void confirmBooking(@RequestBody BookingAnalyticsDTO booking) {
        adminDashboardService.confirmBooking(booking);
    }

    @PostMapping("/users")
    public void addUser(@RequestBody UserAnalyticsDTO user) {
        adminDashboardService.addUser(user);
    }

}
