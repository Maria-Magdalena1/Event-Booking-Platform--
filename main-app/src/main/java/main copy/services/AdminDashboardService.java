package main.services;

import lombok.RequiredArgsConstructor;
import main.microservices.AnalyticsClient;
import main.web.dto.BookingAnalyticsDTO;
import main.web.dto.EventAnalyticsDTO;
import main.web.dto.UserAnalyticsDTO;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {
    private final AnalyticsClient analyticsClient;

    public void addEvent(EventAnalyticsDTO event) {
        analyticsClient.addEvent(event);
    }

    public void confirmBooking(BookingAnalyticsDTO booking) {
        analyticsClient.confirmBooking(booking);
    }

    public void addUser(UserAnalyticsDTO user) {
        analyticsClient.addUser(user);
    }

    public Map<String, Object> getDashboard() {
        return analyticsClient.getDashboard();
    }

}
