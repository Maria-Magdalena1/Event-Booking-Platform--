package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "main")
@EnableFeignClients(basePackages = "main.microservices")
@EnableScheduling
public class EventBookingPlatformApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventBookingPlatformApplication.class, args);
    }
}
