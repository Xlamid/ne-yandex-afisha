package git.xlamid.eventmanagerservice.registration.controller;

import git.xlamid.eventmanagerservice.event.dto.GetEventDto;
import git.xlamid.eventmanagerservice.registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events/registrations")
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/{eventId}")
    public ResponseEntity<Void> registrationUserOnEventByEventId(@PathVariable Long eventId) {
        registrationService.registrationUserOnEventByEventId(eventId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<GetEventDto>> getEventsForUser() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(registrationService.getEventsForUser());
    }

    @DeleteMapping("/cancel/{eventId}")
    public ResponseEntity<Void> cancelRegistrationForUserByEventId(@PathVariable Long eventId) {
        registrationService.cancelRegistrationForUserByEventId(eventId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}