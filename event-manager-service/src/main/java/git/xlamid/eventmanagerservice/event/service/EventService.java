package git.xlamid.eventmanagerservice.event.service;

import git.xlamid.eventmanagerservice.cache.enums.CacheMode;
import git.xlamid.eventmanagerservice.event.dto.CreateEventDto;
import git.xlamid.eventmanagerservice.event.dto.EventSearchRequestDto;
import git.xlamid.eventmanagerservice.event.dto.GetEventDto;
import git.xlamid.eventmanagerservice.event.dto.UpdateEventDto;

import java.util.List;

public interface EventService {

    GetEventDto createEvent(CreateEventDto eventDto);
    List<GetEventDto> searchEventsByFilters(EventSearchRequestDto filterDto);
    List<GetEventDto> getEventsForUser();
    GetEventDto getEventById(Long eventId);
    GetEventDto updateEventById(Long eventId, UpdateEventDto eventDto);
    void deleteEventById(Long eventId);
    CacheMode getCacheMode();
}