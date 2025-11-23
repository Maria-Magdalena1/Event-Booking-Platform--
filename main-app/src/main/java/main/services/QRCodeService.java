package main.services;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
public class QRCodeService {
    public String generateQRCodeBase64(String text, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", stream);
            return Base64.getEncoder().encodeToString(stream.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("Could not generate QR Code", e);
        }
    }
}
