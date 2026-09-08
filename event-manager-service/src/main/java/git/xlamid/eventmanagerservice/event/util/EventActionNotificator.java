package git.xlamid.eventmanagerservice.event.util;

import git.xlamid.eventcommon.kafka.model.enums.EventType;
import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.event.mapper.EventMapper;
import git.xlamid.eventmanagerservice.kafka.sender.NotificationEventSender;
import git.xlamid.eventmanagerservice.user.service.UserSecurityContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

import static git.xlamid.eventcommon.kafka.model.enums.EventType.EVENT_CREATED;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventActionNotificator {

    private final EventMapper eventMapper;
    private final UserSecurityContextService userContextService;
    private final NotificationEventSender notificationEventSender;

    public void applyActionAndNotify(EventEntity eventEntity,
                                      EventType eventType,
                                      boolean isUser,
                                      Consumer<EventEntity> action) {
        Long currentUserId = isUser ? userContextService.getUserIdFromSecurityContext() : null;
        EventEntity oldEventEntity = (eventType.equals(EVENT_CREATED)) ?
                new EventEntity() : eventMapper.copy(eventEntity);

        if (action != null && !eventType.equals(EVENT_CREATED)) {
            action.accept(eventEntity);
        }
        notificationEventSender.sendEvent(
                currentUserId,
                eventType,
                oldEventEntity,
                eventEntity,
                getUserIds(eventEntity)
        );
    }

    private List<Long> getUserIds(EventEntity eventEntity) {
        return eventEntity.getRegistrations().stream()
                .map(registration -> registration.getUser().getId())
                .toList();
    }
}