package git.xlamid.eventmanagerservice.event.service;

import git.xlamid.eventmanagerservice.cache.enums.CacheMode;
import git.xlamid.eventmanagerservice.event.dto.CreateEventDto;
import git.xlamid.eventmanagerservice.event.dto.EventSearchRequestDto;
import git.xlamid.eventmanagerservice.event.dto.GetEventDto;
import git.xlamid.eventmanagerservice.event.dto.UpdateEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpringCacheEventService implements EventService {
    
    private final DbEventService dbEventService;
    
    @Override
    public GetEventDto createEvent(CreateEventDto eventDto) {
        return dbEventService.createEvent(eventDto);
    }

    @Override
    public List<GetEventDto> searchEventsByFilters(EventSearchRequestDto filterDto) {
        return dbEventService.searchEventsByFilters(filterDto);
    }

    @Override
    public List<GetEventDto> getEventsForUser() {
        return dbEventService.getEventsForUser();
    }

    @Override
    @Cacheable(value = "event", key = "#eventId")
    public GetEventDto getEventById(Long eventId) {
        return dbEventService.getEventById(eventId);
    }

    @Override
    @CacheEvict(value = "event", key = "#eventId")
    public GetEventDto updateEventById(Long eventId, UpdateEventDto eventDto) {
        return dbEventService.updateEventById(eventId, eventDto);
    }

    @Override
    @CacheEvict(value = "event", key = "#eventId")
    public void deleteEventById(Long eventId) {
        dbEventService.deleteEventById(eventId);
    }

    @Override
    public CacheMode getCacheMode() {
        return CacheMode.SPRING;
    }
}
