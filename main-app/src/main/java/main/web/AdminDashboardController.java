package main.web;

import lombok.RequiredArgsConstructor;
import main.microservices.AnalyticsClient;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {
    private final AnalyticsClient analyticsClient;

    @GetMapping("/analytics/dashboard")
    public ModelAndView showDashboard() {

        Map<String, Object> data = analyticsClient.getDashboard();

        ModelAndView mav = new ModelAndView("admin/dashboard");
        mav.addObject("totalUsers", data.get("totalUsers"));
        mav.addObject("totalEvents", data.get("totalEvents"));
        mav.addObject("totalBookings", data.get("totalBookings"));
        mav.addObject("totalRevenue", data.get("totalRevenue"));

        List<Map<String, Object>> topEvents = (List<Map<String, Object>>) data.get("topEvents");
        mav.addObject("topEvents", topEvents);

        List<Map<String, Object>> topUsers = (List<Map<String, Object>>) data.get("topUsers");
        mav.addObject("topUsers", topUsers);

        List<Map<String, Object>> seatWarnings = (List<Map<String, Object>>) data.get("seatWarnings");
        mav.addObject("seatWarnings", seatWarnings);

        return mav;
    }

}
