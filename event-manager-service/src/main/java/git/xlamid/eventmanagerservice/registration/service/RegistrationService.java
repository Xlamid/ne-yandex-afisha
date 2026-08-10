package git.xlamid.eventmanagerservice.registration.service;

import git.xlamid.eventmanagerservice.event.dto.GetEventDto;
import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.event.mapper.EventMapper;
import git.xlamid.eventmanagerservice.event.repository.EventRepository;
import git.xlamid.eventmanagerservice.event.util.EventFinder;
import git.xlamid.eventmanagerservice.event.util.EventValidator;
import git.xlamid.eventmanagerservice.exception.model.notfound.RegistrationNotFoundException;
import git.xlamid.eventmanagerservice.registration.entity.RegistrationEntity;
import git.xlamid.eventmanagerservice.registration.repository.RegistrationRepository;
import git.xlamid.eventmanagerservice.user.service.UserSecurityContextService;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import git.xlamid.eventmanagerservice.user.service.UserService;
import jakarta.persistence.LockModeType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final EventFinder eventFinder;
    private final UserService userService;
    private final UserSecurityContextService userContextService;

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EventValidator eventValidator;

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.REPEATABLE_READ)
    public void registrationUserOnEventByEventId(Long eventId) {
        EventEntity eventEntity = eventFinder.findEventById(eventId);
        UserEntity userEntity = userService
                .findUserById(userContextService.getUserIdFromSecurityContext());

        eventValidator.validateRegistrationExists(userEntity.getId(), eventId);
        eventValidator.validateEventForAvailable(eventId, eventEntity.getStatus());
        registrationRepository.save(new RegistrationEntity(
                null,
                OffsetDateTime.now(),
                false,
                userEntity,
                eventEntity
        ));
    }

    public List<GetEventDto> getEventsForUser() {
        Long userId = userContextService.getUserIdFromSecurityContext();
        return eventRepository.findAllByUserId(userId).stream()
                .map(eventMapper::entityToGetDto)
                .toList();
    }

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void cancelRegistrationForUserByEventId(Long eventId) {
        EventEntity eventEntity = eventFinder.findEventById(eventId);
        eventValidator.validateEventForAvailable(eventId, eventEntity.getStatus());

        Long userId = userContextService.getUserIdFromSecurityContext();
        RegistrationEntity regEntity = findRegistrationByUserIdAndEventId(userId, eventId);
        eventValidator.validateRegistrationAlreadyCanceled(regEntity.getId(), regEntity.isCanceled());
        regEntity.setCanceled(true);
        registrationRepository.save(regEntity);
    }

    private RegistrationEntity findRegistrationByUserIdAndEventId(Long userId, Long eventId) {
        return registrationRepository.findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new RegistrationNotFoundException(
                        String.format("Registration with userId: %d and eventId: %d not found", userId, eventId)));
    }
}