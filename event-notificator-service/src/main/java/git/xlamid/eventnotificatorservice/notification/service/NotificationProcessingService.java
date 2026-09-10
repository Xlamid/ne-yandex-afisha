package git.xlamid.eventnotificatorservice.notification.service;

import git.xlamid.eventcommon.kafka.model.NotificationKafkaEvent;
import git.xlamid.eventnotificatorservice.notification.entity.NotificationEntity;
import git.xlamid.eventnotificatorservice.notification.entity.NotificationEventPayloadEntity;
import git.xlamid.eventnotificatorservice.notification.mapper.NotificationEventPayloadMapper;
import git.xlamid.eventnotificatorservice.notification.repository.NotificationEventPayloadRepository;
import git.xlamid.eventnotificatorservice.notification.repository.NotificationRepository;
import git.xlamid.eventnotificatorservice.notification.util.NotificationChangesConverter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationProcessingService {

    private final NotificationRepository notificationRepository;
    private final NotificationEventPayloadRepository payloadRepository;
    private final NotificationEventPayloadMapper payloadMapper;
    private final NotificationChangesConverter notificationChangesConverter;

    @Transactional
    public void processEventForSave(NotificationKafkaEvent kafkaEvent) {
        NotificationEventPayloadEntity payloadEntity = payloadMapper.kafkaEventToEntity(kafkaEvent);
        payloadEntity.setPayload(notificationChangesConverter
                .convertChangesToPayload(kafkaEvent.getChanges()));

        List<NotificationEntity> notificationEntities = kafkaEvent.getSubscribers().stream()
                .map(userId -> new NotificationEntity(
                        null,
                        userId,
                        false,
                        OffsetDateTime.now(ZoneOffset.UTC),
                        null,
                        payloadEntity
                ))
                .toList();

        payloadRepository.save(payloadEntity);
        notificationRepository.saveAll(notificationEntities);
    }
}