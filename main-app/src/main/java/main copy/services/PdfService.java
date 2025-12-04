package main.services;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public File generateBookingPdf(String username,
                                   String email,
                                   String eventName,
                                   LocalDateTime startDate, LocalDateTime endDate,
                                   int seats,
                                   LocalDateTime bookingDate, double totalPrice) throws FileNotFoundException {

        String fileName = "booking_" + username + ".pdf";
        PdfWriter writer = new PdfWriter(fileName);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);


        DeviceRgb backgroundColor = new DeviceRgb(255, 213, 181);
        PdfPage page = pdf.addNewPage();

        PdfCanvas canvas = new PdfCanvas(page);
        canvas.saveState()
                .setFillColor(backgroundColor)
                .rectangle(0, 0, page.getPageSize().getWidth(), page.getPageSize().getHeight())
                .fill()
                .restoreState();

        Paragraph title = new Paragraph("🎫 Booking Confirmation")
                .setBold()
                .setFontSize(24)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(30f);
        document.add(title);

        String safeEventName = (eventName != null && !eventName.isEmpty()) ? eventName : "Unnamed Event";

        Paragraph text = new Paragraph()
                .setFontSize(22)
                .setFontColor(ColorConstants.BLACK)
                .setTextAlignment(TextAlignment.LEFT)
                .add("👤 Name: " + username + "\n")
                .add("Email: " + email + "\n")
                .add("🎟 Event Name: " + safeEventName + "\n")
                .add("🕒 Start Date: " + startDate.format(FORMATTER) + "\n")
                .add("🕓 End Date: " + endDate.format(FORMATTER) + "\n")
                .add("📅 Booking Date: " + bookingDate.format(FORMATTER) + "\n\n")
                .add("Seats Booked: " + seats + "\n")
                .add("Total Price: " + totalPrice + " lv." + "\n")
                .add("✅ Thank you for booking! Enjoy the event.");
        document.add(text);

        document.close();
        return new File(fileName);
    }
}
