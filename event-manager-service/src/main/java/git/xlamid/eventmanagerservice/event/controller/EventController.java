package git.xlamid.eventmanagerservice.event.controller;

import git.xlamid.eventmanagerservice.event.dto.CreateEventDto;
import git.xlamid.eventmanagerservice.event.dto.EventSearchRequestDto;
import git.xlamid.eventmanagerservice.event.dto.GetEventDto;
import git.xlamid.eventmanagerservice.event.dto.UpdateEventDto;
import git.xlamid.eventmanagerservice.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<GetEventDto> createEvent(@Valid @RequestBody CreateEventDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventService.createEvent(dto));
    }

    @PostMapping("/search")
    public ResponseEntity<List<GetEventDto>> searchEventsByFilters(@Valid @RequestBody EventSearchRequestDto dto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.searchEventsByFilters(dto));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GetEventDto>> getEventsForUser() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.getEventsForUser());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<GetEventDto> getEventById(@PathVariable Long eventId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.getEventById(eventId));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<GetEventDto> updateEventById(@PathVariable Long eventId,
                                                       @Valid @RequestBody UpdateEventDto dto) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.updateEventById(eventId, dto));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<GetEventDto> deleteEventById(@PathVariable Long eventId) {
        eventService.deleteEventById(eventId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}