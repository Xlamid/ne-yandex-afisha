package git.xlamid.eventnotificatorservice.kafka;

import git.xlamid.eventcommon.kafka.model.FieldChange;
import git.xlamid.eventcommon.kafka.model.NotificationKafkaEvent;
import git.xlamid.eventcommon.kafka.model.enums.EventType;
import git.xlamid.eventnotificatorservice.EventNotificatorAbstractWithContainerTest;
import git.xlamid.eventnotificatorservice.notification.entity.NotificationEntity;
import git.xlamid.eventnotificatorservice.notification.entity.NotificationEventPayloadEntity;
import git.xlamid.eventnotificatorservice.notification.repository.NotificationEventPayloadRepository;
import git.xlamid.eventnotificatorservice.notification.repository.NotificationRepository;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class EventConsumerKafkaIntegrationTest extends EventNotificatorAbstractWithContainerTest {

    @Value("${spring.kafka.topic.notification}")
    private String topicName;

    @Autowired
    private KafkaTemplate<Long, NotificationKafkaEvent> kafkaTemplate;
    @Autowired
    private NotificationEventPayloadRepository payloadRepository;
    @Autowired
    private NotificationRepository notificationRepository;

    @BeforeEach
    public void setup() {
        notificationRepository.deleteAll();
        payloadRepository.deleteAll();
    }

    @Test
    void shouldProcessEventAndSavePayloadWithNotifications() throws Exception {
        // Arrange
        UUID messageId = UUID.randomUUID();
        List<FieldChange> changes = List.of(
                new FieldChange("status", "WAIT_START", "STARTED"),
                new FieldChange("cost", 100, 200)
        );
        List<Long> subscribers = List.of(10L, 20L, 30L);
        NotificationKafkaEvent event = new NotificationKafkaEvent(
                messageId,
                EventType.EVENT_UPDATED,
                1L,
                OffsetDateTime.now(ZoneOffset.UTC),
                5L,
                5L,
                subscribers,
                changes
        );

        // Act
        kafkaTemplate.send(topicName, event.getOwnerId(), event).get();
        awaitUntilPayloadIsSaved(messageId);

        // Assert
        List<NotificationEventPayloadEntity> payloads = payloadRepository.findAll();
        assertEquals(1, payloads.size());

        NotificationEventPayloadEntity savedPayload = payloads.getFirst();
        assertEquals(messageId, savedPayload.getMessageId());

        // Check NotificationChangesConverter
        Map<String, Object> payloadMap = savedPayload.getPayload();
        assertNotNull(payloadMap);
        assertTrue(payloadMap.containsKey("status"));
        assertTrue(payloadMap.containsKey("cost"));

        // Check NotificationProcessingService
        List<NotificationEntity> notifications = notificationRepository.findAll();
        assertEquals(3, notifications.size());

        assertTrue(notifications.stream().anyMatch(n -> n.getUserId().equals(10L)));
        assertTrue(notifications.stream().anyMatch(n -> n.getUserId().equals(30L)));
        assertTrue(notifications.stream().noneMatch(NotificationEntity::getIsRead));
    }

    @Test
    void shouldHandleDuplicateMessagesIdempotently() throws Exception {
        // Arrange
        UUID messageId = UUID.randomUUID();
        NotificationKafkaEvent event = new NotificationKafkaEvent(
                messageId,
                EventType.EVENT_CREATED,
                2L,
                OffsetDateTime.now(ZoneOffset.UTC),
                5L,
                5L,
                List.of(10L),
                List.of()
        );

        // Act
        kafkaTemplate.send(topicName, event.getOwnerId(), event).get();
        kafkaTemplate.send(topicName, event.getOwnerId(), event).get();
        Thread.sleep(200);

        // Assert
        List<NotificationEventPayloadEntity> payloads = payloadRepository.findAll();
        List<NotificationEntity> notifications = notificationRepository.findAll();

        assertEquals(1, payloads.size());
        assertEquals(1, notifications.size());
    }

    private void awaitUntilPayloadIsSaved(UUID messageId) throws InterruptedException {
        for (int i = 5; i > 0; i--) {
            if (payloadRepository.existsByMessageId(messageId)) {
                return;
            }
            Thread.sleep(200);
        }
        throw new AssertionError(
                String.format("Listener didn't manage to save data in time for messageId: %s", messageId)
        );
    }
}