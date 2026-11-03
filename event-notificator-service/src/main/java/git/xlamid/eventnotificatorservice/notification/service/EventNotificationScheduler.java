package git.xlamid.eventnotificatorservice.notification.service;

import git.xlamid.eventnotificatorservice.notification.entity.NotificationEntity;
import git.xlamid.eventnotificatorservice.notification.entity.NotificationEventPayloadEntity;
import git.xlamid.eventnotificatorservice.notification.repository.NotificationEventPayloadRepository;
import git.xlamid.eventnotificatorservice.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventNotificationScheduler {

    private final NotificationRepository notificationRepository;
    private final NotificationEventPayloadRepository payloadRepository;

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Scheduled(fixedRateString = "${scheduled.fixed-rate.notifications}")
    public void clearOldAndReadNotifications() {
        OffsetDateTime threshold = OffsetDateTime.now().minusDays(7);
        List<NotificationEntity> notificationEntities = notificationRepository
                .findAllOldAndReadNotifications(threshold);
        List<NotificationEventPayloadEntity> payloadEntities = payloadRepository
                .findAllByNotificationsIsNull();

        if (notificationEntities != null && !notificationEntities.isEmpty()) {
            List<Long> notificationIds = notificationEntities.stream().map(NotificationEntity::getId).toList();
            log.info("Clearing old notifications: {}", notificationIds);
            notificationRepository.deleteAll(notificationEntities);
        }
        if (payloadEntities != null && !payloadEntities.isEmpty()) {
            List<Long> payloadIds = payloadEntities.stream().map(NotificationEventPayloadEntity::getId).toList();
            log.info("Clearing old payloads: {}", payloadIds);
            payloadRepository.deleteAll(payloadEntities);
        }
    }
}