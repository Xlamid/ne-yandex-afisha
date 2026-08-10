package git.xlamid.eventmanagerservice.event.service;

import git.xlamid.eventmanagerservice.event.dto.CreateEventDto;
import git.xlamid.eventmanagerservice.event.dto.EventSearchRequestDto;
import git.xlamid.eventmanagerservice.event.dto.GetEventDto;
import git.xlamid.eventmanagerservice.event.dto.UpdateEventDto;
import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.event.mapper.EventMapper;
import git.xlamid.eventmanagerservice.event.model.enums.EventStatus;
import git.xlamid.eventmanagerservice.event.repository.EventRepository;
import git.xlamid.eventmanagerservice.event.util.EventFinder;
import git.xlamid.eventmanagerservice.event.util.EventSorter;
import git.xlamid.eventmanagerservice.event.util.EventSpecification;
import git.xlamid.eventmanagerservice.event.util.EventValidator;
import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import git.xlamid.eventmanagerservice.location.service.LocationService;
import git.xlamid.eventmanagerservice.registration.service.RegistrationService;
import git.xlamid.eventmanagerservice.user.service.UserSecurityContextService;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import git.xlamid.eventmanagerservice.user.service.UserService;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EventValidator eventValidator;
    private final EventFinder eventFinder;
    private final EventSpecification eventSpecification;
    private final EventSorter eventSorter;

    private final LocationService locationService;
    private final UserSecurityContextService userContextService;
    private final UserService userService;
    private final RegistrationService registrationService;

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public GetEventDto createEvent(CreateEventDto eventDto) {
        LocationEntity locationEntity = locationService.findLocationById(eventDto.getLocationId());
        UserEntity userEntity = userService
                .findUserById(userContextService.getUserIdFromSecurityContext());

        eventValidator.validateEarlyDate(eventDto.getDate());
        eventValidator.validateMaxPlacesForLocation
                (eventDto.getMaxPlaces(), locationEntity.getCapacity());

        EventEntity eventEntity = eventMapper.dtoToEntity(eventDto);
        eventEntity.setStatus(EventStatus.WAIT_START.name());
        eventEntity.setLocation(locationEntity);
        eventEntity.setUser(userEntity);

        GetEventDto res = eventMapper.entityToGetDto(
                eventRepository.save(eventEntity)
        );
        registrationService.registrationUserOnEventByEventId(eventEntity.getId());
        return res;
    }

    public List<GetEventDto> searchEventsByFilters(EventSearchRequestDto filterDto) {
        Specification<EventEntity> spec = eventSpecification.setSpecification(filterDto);
        Sort sort = eventSorter.getSorts();
        return eventRepository.findAll(spec, sort).stream()
                .map(eventMapper::entityToGetDto)
                .toList();
    }

    public List<GetEventDto> getEventsForUser() {
        Long userId = userContextService.getUserIdFromSecurityContext();
        return eventRepository
                .findAllByUserIdWithLocationIdAndUserId(userId).stream()
                .map(eventMapper::entityToGetDto)
                .toList();
    }

    public GetEventDto getEventById(Long eventId) {
        return eventMapper.entityToGetDto(
                eventFinder.findEventById(eventId)
        );
    }

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public GetEventDto updateEventById(Long eventId, UpdateEventDto eventDto) {
        EventEntity eventEntity = eventFinder.findEventById(eventId);
        LocationEntity locationEntity = locationService.findLocationById(eventDto.getLocationId());

        eventValidator.validateAccess(eventEntity.getUser().getId());
        eventValidator.validateEarlyDate(eventDto.getDate());
        eventValidator.validateMaxPlacesForOccupiedPlaces
                (eventDto.getMaxPlaces(), eventEntity.getOccupiedPlaces());
        eventValidator.validateMaxPlacesForLocation
                (eventDto.getMaxPlaces(), locationEntity.getCapacity());

        eventMapper.updateEntityByDto(eventEntity, eventDto);
        return eventMapper.entityToGetDto(
                eventRepository.save(eventEntity)
        );
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void deleteEventById(Long eventId) {
        EventEntity eventEntity = eventFinder.findEventById(eventId);
        eventValidator.validateAccess(eventEntity.getUser().getId());
        eventValidator.validateEventForAvailable(eventId, eventEntity.getStatus());

        eventEntity.setStatus(EventStatus.CANCELLED.name());
        eventRepository.save(eventEntity);
    }
}