package git.xlamid.eventmanagerservice.event.service;

import git.xlamid.eventmanagerservice.cache.enums.CacheMode;
import git.xlamid.eventmanagerservice.event.dto.CreateEventDto;
import git.xlamid.eventmanagerservice.event.dto.EventSearchRequestDto;
import git.xlamid.eventmanagerservice.event.dto.GetEventDto;
import git.xlamid.eventmanagerservice.event.dto.UpdateEventDto;
import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.event.mapper.EventMapper;
import git.xlamid.eventmanagerservice.event.repository.EventRepository;
import git.xlamid.eventmanagerservice.event.util.*;
import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import git.xlamid.eventmanagerservice.location.service.DbLocationService;
import git.xlamid.eventmanagerservice.registration.service.RegistrationService;
import git.xlamid.eventmanagerservice.user.service.UserSecurityContextService;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import git.xlamid.eventmanagerservice.user.service.UserService;
import jakarta.persistence.LockModeType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static git.xlamid.eventcommon.kafka.model.enums.EventType.*;
import static git.xlamid.eventmanagerservice.event.model.enums.EventStatus.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DbEventService implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EventValidator eventValidator;
    private final EventFinder eventFinder;
    private final EventSpecification eventSpecification;
    private final EventSorter eventSorter;
    private final EventActionNotificator eventActionNotificator;

    private final DbLocationService dbLocationService;
    private final UserSecurityContextService userContextService;
    private final UserService userService;
    private final RegistrationService registrationService;

    @Override
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public GetEventDto createEvent(CreateEventDto eventDto) {
        Long currentUserId = userContextService.getUserIdFromSecurityContext();
        LocationEntity locationEntity = dbLocationService.findLocationById(eventDto.getLocationId());
        UserEntity userEntity = userService.findUserById(currentUserId);

        eventValidator.validateEarlyDate(eventDto.getDate());
        eventValidator.validateMaxPlacesForLocation
                (eventDto.getMaxPlaces(), locationEntity.getCapacity());

        EventEntity eventEntity = eventMapper.dtoToEntity(eventDto);
        eventEntity.setStatus(WAIT_START.name());
        eventEntity.setLocation(locationEntity);
        eventEntity.setOwner(userEntity);

        GetEventDto res = eventMapper.entityToGetDto(
                eventRepository.save(eventEntity)
        );
        registrationService.registrationUserOnEventByEventId(eventEntity.getId());

        eventActionNotificator.applyActionAndNotify(eventEntity, EVENT_CREATED, true, null);
        return res;
    }

    @Override
    public List<GetEventDto> searchEventsByFilters(EventSearchRequestDto filterDto) {
        Specification<EventEntity> spec = eventSpecification.setSpecification(filterDto);
        Sort sort = eventSorter.getSorts();
        return eventRepository.findAll(spec, sort).stream()
                .map(eventMapper::entityToGetDto)
                .toList();
    }

    @Override
    public List<GetEventDto> getEventsForUser() {
        Long userId = userContextService.getUserIdFromSecurityContext();
        return eventRepository
                .findAllByUserIdWithLocationIdAndUserId(userId).stream()
                .map(eventMapper::entityToGetDto)
                .toList();
    }

    @Override
    public GetEventDto getEventById(Long eventId) {
        return eventMapper.entityToGetDto(
                eventFinder.findEventById(eventId)
        );
    }

    @Override
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public GetEventDto updateEventById(Long eventId, UpdateEventDto eventDto) {
        EventEntity eventEntity = eventFinder.findEventById(eventId);
        LocationEntity locationEntity = dbLocationService.findLocationById(eventDto.getLocationId());

        eventValidator.validateAccess(eventEntity.getOwner());
        eventValidator.validateEarlyDate(eventDto.getDate());
        eventValidator.validateMaxPlacesForOccupiedPlaces
                (eventDto.getMaxPlaces(), eventEntity.getOccupiedPlaces());
        eventValidator.validateMaxPlacesForLocation
                (eventDto.getMaxPlaces(), locationEntity.getCapacity());

        eventActionNotificator.applyActionAndNotify(eventEntity, EVENT_UPDATED, true,
                eEntity -> eventMapper.updateEntityByDto(eEntity, eventDto));
        return eventMapper.entityToGetDto(
                eventRepository.save(eventEntity)
        );
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void deleteEventById(Long eventId) {
        EventEntity eventEntity = eventFinder.findEventById(eventId);
        eventValidator.validateAccess(eventEntity.getOwner());
        eventValidator.validateEventForAvailable(eventId, eventEntity.getStatus());

        eventActionNotificator.applyActionAndNotify(eventEntity, EVENT_CANCELLED, true,
                eEntity -> eEntity.setStatus(CANCELLED.name()));
        eventRepository.save(eventEntity);
    }
    
    @Override
    public CacheMode getCacheMode() {
        return CacheMode.DB;
    }
}