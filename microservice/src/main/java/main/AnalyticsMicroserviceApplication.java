package main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication

public class AnalyticsMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsMicroserviceApplication.class, args);
    }

}
