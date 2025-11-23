package main.services;

import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
public class BookingReminderService {

    //private final Calendar calendar;

    //public BookingReminderService() throws Exception {
    //    this.calendar = GoogleCalendarService.getCalendarService();
    //}

    //public Event createBookingReminder(String title, String description,
    //                                   String startISO, String endISO,
    //                                   int popupMinutes, int emailMinutes) throws Exception {
//
    //    Event event = new Event()
    //            .setSummary(title)
    //            .setDescription(description)
    //            .setStart(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(startISO)))
    //            .setEnd(new EventDateTime().setDateTime(new com.google.api.client.util.DateTime(endISO)))
    //            .setReminders(new Event.Reminders()
    //                    .setUseDefault(false)
    //                    .setOverrides(Arrays.asList(
    //                            new EventReminder().setMethod("popup").setMinutes(popupMinutes),
    //                            new EventReminder().setMethod("email").setMinutes(emailMinutes)
    //                    )));
//
    //    return calendar.events().insert("primary", event).execute();
    //}

    private static final DateTimeFormatter CALENDAR_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    public String getGoogleCalendarLink(String title, String description,
                                        LocalDateTime start, LocalDateTime end) {
        String startStr = start.atZone(ZoneOffset.UTC).format(CALENDAR_FORMATTER);
        String endStr = end.atZone(ZoneOffset.UTC).format(CALENDAR_FORMATTER);

        String titleEncoded = title.replace(" ", "+");
        String descriptionEncoded = description.replace(" ", "+");

        //String titleEncoded = URLEncoder.encode(title, StandardCharsets.UTF_8);
        //String descriptionEncoded = URLEncoder.encode(description, StandardCharsets.UTF_8);
        return "https://calendar.google.com/calendar/r/eventedit?"
                + "text=" + titleEncoded
                + "&dates=" + startStr + "/" + endStr
                + "&details=" + descriptionEncoded;
    }
}
