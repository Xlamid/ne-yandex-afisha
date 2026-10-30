package git.xlamid.eventmanagerservice.event.controller;

import git.xlamid.eventmanagerservice.cache.enums.CacheMode;
import git.xlamid.eventmanagerservice.event.dto.CreateEventDto;
import git.xlamid.eventmanagerservice.event.dto.EventSearchRequestDto;
import git.xlamid.eventmanagerservice.event.dto.GetEventDto;
import git.xlamid.eventmanagerservice.event.dto.UpdateEventDto;
import git.xlamid.eventmanagerservice.event.service.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/events")
public class EventController {

    private final Map<CacheMode, EventService> eventServices;

    @Autowired
    public EventController(List<EventService> eventServices) {
        this.eventServices = eventServices.stream()
                .collect(Collectors.toMap(EventService::getCacheMode, Function.identity()));
    }

    @PostMapping
    public ResponseEntity<GetEventDto> createEvent(@Valid @RequestBody CreateEventDto dto,
                                                   @RequestParam(defaultValue = "DB") CacheMode cacheMode) {
        log.info("Create event {}", dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventServices.get(cacheMode).createEvent(dto));
    }

    @PostMapping("/search")
    public ResponseEntity<List<GetEventDto>> searchEventsByFilters(@Valid @RequestBody EventSearchRequestDto dto,
                                                                   @RequestParam(defaultValue = "DB") CacheMode cacheMode) {
        log.info("Search events by filters {}", dto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventServices.get(cacheMode).searchEventsByFilters(dto));
    }

    @GetMapping("/my")
    public ResponseEntity<List<GetEventDto>> getEventsForUser(@RequestParam(defaultValue = "DB") CacheMode cacheMode) {
        log.info("Get events for user");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventServices.get(cacheMode).getEventsForUser());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<GetEventDto> getEventById(@PathVariable Long eventId,
                                                    @RequestParam(defaultValue = "DB") CacheMode cacheMode) {
        log.info("Get event by id {}", eventId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventServices.get(cacheMode).getEventById(eventId));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<GetEventDto> updateEventById(@PathVariable Long eventId,
                                                       @Valid @RequestBody UpdateEventDto dto,
                                                       @RequestParam(defaultValue = "DB") CacheMode cacheMode) {
        log.info("Update event by id {}", eventId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventServices.get(cacheMode).updateEventById(eventId, dto));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<GetEventDto> deleteEventById(@PathVariable Long eventId,
                                                       @RequestParam(defaultValue = "DB") CacheMode cacheMode) {
        log.info("Delete event by id {}", eventId);
        eventServices.get(cacheMode).deleteEventById(eventId);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}