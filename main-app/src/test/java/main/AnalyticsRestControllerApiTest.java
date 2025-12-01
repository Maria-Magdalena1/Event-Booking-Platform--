package main;

import main.services.AdminDashboardService;
import main.web.AnalyticsRestController;
import main.web.dto.BookingAnalyticsDTO;
import main.web.dto.EventAnalyticsDTO;
import main.web.dto.UserAnalyticsDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalyticsRestController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AnalyticsRestControllerApiTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminDashboardService adminDashboardService;

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetDashboard() throws Exception {
        Map<String, Object> dashboard = Map.of(
                "userCount", 5,
                "eventCount", 3,
                "bookingCount", 10
        );

        when(adminDashboardService.getDashboard()).thenReturn(dashboard);

        mockMvc.perform(get("/api/dashboard")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userCount").value(5))
                .andExpect(jsonPath("$.eventCount").value(3))
                .andExpect(jsonPath("$.bookingCount").value(10));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAddUser() throws Exception {
        String userJson = """
                {"username":"testUser","email":"test@test.com"}
                """;

        mockMvc.perform(post("/api/users")
                        .contentType("application/json")
                        .content(userJson)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(adminDashboardService).addUser(any(UserAnalyticsDTO.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAddEvent() throws Exception {
        String eventJson = """
                {"name":"Concert","totalSeats":100,"price":50.0}
                """;

        mockMvc.perform(post("/api/events")
                        .contentType("application/json")
                        .content(eventJson)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(adminDashboardService).addEvent(any(EventAnalyticsDTO.class));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testConfirmBooking() throws Exception {
        String bookingJson = """
                {"eventId":"123e4567-e89b-12d3-a456-426614174000","userId":"123e4567-e89b-12d3-a456-426614174001","seatsBooked":2,"totalPrice":100.0}
                """;

        mockMvc.perform(post("/api/bookings")
                        .contentType("application/json")
                        .content(bookingJson)
                        .with(csrf()))
                .andExpect(status().isOk());

        verify(adminDashboardService).confirmBooking(any(BookingAnalyticsDTO.class));
    }
}
