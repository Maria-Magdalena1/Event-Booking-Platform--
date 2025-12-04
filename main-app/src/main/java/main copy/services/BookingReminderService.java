package main.services;

import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Service
public class BookingReminderService {

    private static final DateTimeFormatter CALENDAR_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public String getGoogleCalendarLink(String title, String description,
                                        LocalDateTime start, LocalDateTime end) {
        String startStr = start.atZone(ZoneOffset.UTC).format(CALENDAR_FORMATTER);
        String endStr = end.atZone(ZoneOffset.UTC).format(CALENDAR_FORMATTER);

        String titleEncoded = URLEncoder.encode(title, StandardCharsets.UTF_8);
        String descriptionEncoded = URLEncoder.encode(description, StandardCharsets.UTF_8);
        return "https://calendar.google.com/calendar/r/eventedit?"
                + "text=" + titleEncoded
                + "&dates=" + startStr + "/" + endStr
                + "&details=" + descriptionEncoded;
    }
}
