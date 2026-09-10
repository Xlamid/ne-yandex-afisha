package git.xlamid.eventnotificatorservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableScheduling
@EnableMethodSecurity
@SpringBootApplication
public class EventNotificatorServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventNotificatorServiceApplication.class, args);
    }
}