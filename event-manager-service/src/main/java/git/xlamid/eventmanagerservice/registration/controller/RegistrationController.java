package git.xlamid.eventmanagerservice.registration.controller;

import git.xlamid.eventmanagerservice.event.dto.GetEventDto;
import git.xlamid.eventmanagerservice.registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/{eventId}")
    public ResponseEntity<Void> registrationUserOnEventByEventId(@PathVariable Long eventId) {
        log.info("Register user on event with event id {}", eventId);
        registrationService.registrationUserOnEventByEventId(eventId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<GetEventDto>> getEventsForUser() {
        log.info("Get events for user");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(registrationService.getEventsForUser());
    }

    @DeleteMapping("/cancel/{eventId}")
    public ResponseEntity<Void> cancelRegistrationForUserByEventId(@PathVariable Long eventId) {
        log.info("Cancel registration for user with event id {}", eventId);
        registrationService.cancelRegistrationForUserByEventId(eventId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}