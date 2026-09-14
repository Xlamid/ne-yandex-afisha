package git.xlamid.eventmanagerservice.kafka.sender;

import git.xlamid.eventcommon.kafka.model.FieldChange;
import git.xlamid.eventcommon.kafka.model.NotificationKafkaEvent;
import git.xlamid.eventcommon.kafka.model.enums.EventType;
import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.kafka.util.EventChangesSetter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.SendResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class NotificationEventSender {

    private final KafkaTemplate<Long, NotificationKafkaEvent> kafkaTemplate;
    private final EventChangesSetter eventChangesSetter;
    private final String topic;

    public NotificationEventSender(KafkaTemplate<Long, NotificationKafkaEvent> kafkaTemplate,
                                   EventChangesSetter eventChangesSetter,
                                   @Value("${spring.kafka.topic.notification}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.eventChangesSetter = eventChangesSetter;
        this.topic = topic;
    }

    public void sendEvent(Long changedById,
                          EventType eventType,
                          EventEntity oldEventEntity,
                          EventEntity newEventEntity,
                          List<Long> subscribers) {
        UUID messageId = UUID.randomUUID();
        OffsetDateTime occurredAt = OffsetDateTime.now();
        List<FieldChange> changes = eventChangesSetter.setAndGetChanges(oldEventEntity, newEventEntity);
        sendEvent(new NotificationKafkaEvent(
                messageId,
                eventType,
                newEventEntity.getId(),
                occurredAt,
                newEventEntity.getOwner().getId(),
                changedById,
                subscribers,
                changes
        ));
    }

    private void sendEvent(NotificationKafkaEvent event) {
        log.info("Sending notification event to kafka: {}", event);
        CompletableFuture<SendResult<Long, NotificationKafkaEvent>> res =
                kafkaTemplate.send(
                        topic,
                        event.getOwnerId(),
                        event
                );
        res.thenAccept(sendRes ->
            log.info("Successful notification event sent to kafka: {} {}", event, sendRes))
                .exceptionally(e -> {
                    log.error("Failed notification event sent to kafka: {}", event, e);
                    return null;
                });
    }
}