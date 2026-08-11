package git.xlamid.eventmanagerservice.event.controller;

import git.xlamid.eventmanagerservice.event.dto.CreateEventDto;
import git.xlamid.eventmanagerservice.event.dto.EventSearchRequestDto;
import git.xlamid.eventmanagerservice.event.dto.GetEventDto;
import git.xlamid.eventmanagerservice.event.dto.UpdateEventDto;
import git.xlamid.eventmanagerservice.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<GetEventDto> createEvent(@Valid @RequestBody CreateEventDto dto) {
        log.info("Create event {}", dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventService.createEvent(dto));
    }

    @PostMapping("/search")
    public ResponseEntity<List<GetEventDto>> searchEventsByFilters(@Valid @RequestBody EventSearchRequestDto dto) {
        log.info("Search events by filters {}", dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.searchEventsByFilters(dto));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GetEventDto>> getEventsForUser() {
        log.info("Get events for user");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.getEventsForUser());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<GetEventDto> getEventById(@PathVariable Long eventId) {
        log.info("Get event by id {}", eventId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.getEventById(eventId));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<GetEventDto> updateEventById(@PathVariable Long eventId,
                                                       @Valid @RequestBody UpdateEventDto dto) {
        log.info("Update event by id {}", eventId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.updateEventById(eventId, dto));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<GetEventDto> deleteEventById(@PathVariable Long eventId) {
        log.info("Delete event by id {}", eventId);
        eventService.deleteEventById(eventId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}