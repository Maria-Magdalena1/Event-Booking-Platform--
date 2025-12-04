package main.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class QRCodeService {
    public String generateQRCodeBase64(String text) {
        try {
            String url = "https://api.qrserver.com/v1/create-qr-code/?data="
                    + java.net.URLEncoder.encode(text, StandardCharsets.UTF_8)
                    + "&size=250x250";

            RestTemplate restTemplate = new RestTemplate();
            byte[] imageBytes = restTemplate.getForObject(url, byte[].class);
            return Base64.getEncoder().encodeToString(imageBytes);

        } catch (Exception e) {
            throw new RuntimeException("Could not generate QR Code", e);
        }
    }
}
