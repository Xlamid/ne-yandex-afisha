package git.xlamid.eventmanagerservice.util;

import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.event.model.enums.EventStatus;
import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;

@Component
public class EventCreator {

    public EventEntity createEventEntity(String name,
                                         UserEntity owner,
                                         EventStatus status,
                                         LocationEntity location) {
        return createCustomEventEntity(
                name,
                OffsetDateTime.now().plusDays(5),
                100,
                60,
                status,
                owner,
                location
        );
    }

    public EventEntity createCustomEventEntity(String name,
                                               OffsetDateTime date,
                                               int cost,
                                               int duration,
                                               EventStatus status,
                                               UserEntity owner,
                                               LocationEntity location) {
        return new EventEntity(
                null,
                name,
                date,
                cost,
                duration,
                50,
                0,
                status.name(),
                location,
                owner,
                new ArrayList<>()
        );
    }
}