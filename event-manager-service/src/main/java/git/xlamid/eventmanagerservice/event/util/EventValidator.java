package git.xlamid.eventmanagerservice.event.util;

import git.xlamid.eventmanagerservice.event.model.enums.EventStatus;
import git.xlamid.eventmanagerservice.exception.model.exists.RegistrationExistsException;
import git.xlamid.eventmanagerservice.exception.model.validation.AlreadyValidationException;
import git.xlamid.eventmanagerservice.exception.model.validation.IncorrectValidationException;
import git.xlamid.eventmanagerservice.exception.model.validation.TooManyValidationException;
import git.xlamid.eventmanagerservice.exception.model.validation.UnavailableValidationException;
import git.xlamid.eventmanagerservice.registration.entity.RegistrationEntity;
import git.xlamid.eventmanagerservice.registration.repository.RegistrationRepository;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import git.xlamid.eventmanagerservice.user.model.enums.UserModel;
import git.xlamid.eventmanagerservice.user.model.enums.UserRole;
import git.xlamid.eventmanagerservice.user.service.UserSecurityContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import static git.xlamid.eventmanagerservice.event.model.enums.EventStatus.*;

@Component
@RequiredArgsConstructor
public class EventValidator {

    private static final Integer LOCK_DATE_HOURS = 24;

    private static final Map<EventStatus, String> UNAVAILABLE_STATUSES = Map.of(
            STARTED, "Event with id %s is already started",
            CANCELLED, "Event with id %s is already cancelled",
            FINISHED, "Event with id %s is already finished"
    );

    private final UserSecurityContextService userContextService;
    private final RegistrationRepository registrationRepository;

    public void validateMaxPlacesForLocation(Integer maxPlaces, Integer capacity) {
        if (maxPlaces > capacity) {
            throw new TooManyValidationException(String.format(
                    "Max places in the event '%d' is greater than in the location '%d'",
                    maxPlaces, capacity));
        }
    }

    public void validateMaxPlacesForOccupiedPlaces(Integer maxPlaces, Integer occupiedPlaces) {
        if (maxPlaces < occupiedPlaces) {
            throw new TooManyValidationException(String.format(
                    "Max places in the event '%d' is less than already occupied places '%d'",
                    maxPlaces, occupiedPlaces));
        }
    }

    public void validateEarlyDate(OffsetDateTime dateTime) {
        OffsetDateTime now = OffsetDateTime.now().plusHours(LOCK_DATE_HOURS);
        if (dateTime.isBefore(now)) {
            throw new IncorrectValidationException
                    ("Event must not start earlier than " + LOCK_DATE_HOURS + " hours");
        }
    }

    public void validateEventForAvailable(Long eventId, String status) {
        EventStatus eventStatus = EventStatus.valueOf(status);
        UNAVAILABLE_STATUSES.forEach((uStatus, message) -> {
            if (uStatus.equals(eventStatus)) {
                throw new UnavailableValidationException(String.format(message, eventId));
            }
        });
    }

    public void validateAccess(UserEntity owner) {
        UserModel userModel = userContextService.getUserFromSecurityContext();
        boolean isUserOwner = owner.getId().equals(userModel.getId());
        if (userModel.getRole().equals(UserRole.USER) && !isUserOwner) {
            throw new AccessDeniedException("No access rights to modify the event");
        }
    }

    public void validateRegistrationExists(Long userId, Long eventId) {
        Optional<RegistrationEntity> regEntity =
                registrationRepository.findByUserIdAndEventId(userId, eventId);
        if (regEntity.isPresent()) {
            throw new RegistrationExistsException(
                    String.format("Registration %d for event with id: %d and user with id: %d already exists",
                            regEntity.get().getId(), eventId, userId)
            );
        }
    }

    public void validateRegistrationAlreadyCanceled(Long registrationId, boolean isCanceled) {
        if (isCanceled) {
            throw new AlreadyValidationException("Registration with id: " + registrationId + " has been cancelled");
        }
    }
}