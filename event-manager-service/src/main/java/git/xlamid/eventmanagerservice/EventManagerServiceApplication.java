package git.xlamid.eventmanagerservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableMethodSecurity
@SpringBootApplication
public class EventManagerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventManagerServiceApplication.class, args);
    }
}