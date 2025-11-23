package main.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import main.entities.Event;
import main.entities.User;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {
    //private final JavaMailSender mailSender;
//
    //public NotificationService(JavaMailSender mailSender) {
    //    this.mailSender = mailSender;
    //}

    //public void sendEmailReminder(Event event, List<User> users, boolean isBookingReminder) {
    //    for (User user : users) {
    //        try {
    //            MimeMessage message = mailSender.createMimeMessage();
    //            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
    //            helper.setFrom("");
    //            helper.setTo(user.getEmail());
    //            helper.setSubject(isBookingReminder ?
    //                    "Don't miss the event: " + event.getName() :
    //                    "Reminder: Your booked event " + event.getName());
//
    //            String htmlText = getHtmlText(event, isBookingReminder, user);
    //            helper.setText(htmlText, true);
//
    //            mailSender.send(message);
    //        } catch (MessagingException e) {
    //            e.printStackTrace();
    //        }
    //    }
    //}

    private static String getHtmlText(Event event, boolean isBookingReminder, User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"); // human-friendly
        String formattedDate = event.getStartDate().format(formatter);

        String text = "<html><body>";
        text += "<h2>Hello " + user.getUsername() + ",</h2>";

        if (isBookingReminder) {
            text += "<p>An upcoming event <strong>\"" + event.getName() + "\"</strong> will take place on <strong>"
                    + formattedDate + "</strong> at <strong>" + event.getLocation() + "</strong>.</p>";
            text += "<p>You haven't booked yet! <a href='#'>Secure your spot now</a>.</p>";
        } else {
            text += "<p>This is a reminder that your booked event <strong>\"" + event.getName() + "\"</strong> "
                    + "is happening on <strong>" + formattedDate + "</strong> at <strong>" + event.getLocation() + "</strong>.</p>";
        }

        text += "<br><p>Event Booking Platform</p>";
        text += "</body></html>";
        return text;
    }
}
