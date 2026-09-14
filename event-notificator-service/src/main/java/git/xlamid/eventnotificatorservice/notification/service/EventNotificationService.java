package git.xlamid.eventnotificatorservice.notification.service;

import git.xlamid.eventcommon.kafka.model.enums.EventType;
import git.xlamid.eventnotificatorservice.exception.model.validation.EmptyValidationException;
import git.xlamid.eventnotificatorservice.notification.dto.GetNotificationDto;
import git.xlamid.eventnotificatorservice.notification.dto.MarkNotificationDto;
import git.xlamid.eventnotificatorservice.notification.entity.NotificationEntity;
import git.xlamid.eventnotificatorservice.notification.entity.NotificationEventPayloadEntity;
import git.xlamid.eventnotificatorservice.notification.mapper.NotificationMapper;
import git.xlamid.eventnotificatorservice.notification.repository.NotificationEventPayloadRepository;
import git.xlamid.eventnotificatorservice.notification.repository.NotificationRepository;
import git.xlamid.eventnotificatorservice.security.service.UserSecurityContextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventNotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationEventPayloadRepository payloadRepository;
    private final NotificationMapper notificationMapper;

    private final UserSecurityContextService userSecurityContextService;

    public List<GetNotificationDto> getUnreadNotifications() {
        Long userId = userSecurityContextService.getUserIdFromSecurityContext();
        List<NotificationEntity> notificationWithPayloadEntities = notificationRepository
                .findAllNotificationsByUserIdAndIsReadFalse(userId);

        return notificationWithPayloadEntities.stream()
                .map(notification -> {
                    GetNotificationDto notificationDto = notificationMapper.entityToGetDto(notification);
                    EventType eventType = EventType.valueOf(notification.getPayload().getEventType());
                    notificationDto.setMessage(eventType.getMessage());
                    return notificationDto;
                })
                .toList();
    }

    @Transactional
    public void markNotificationsAsRead(MarkNotificationDto notificationDto) {
        Long userId = userSecurityContextService.getUserIdFromSecurityContext();
        List<NotificationEntity> notificationEntities = notificationRepository
                .findAllByUserIdAndIsReadFalseAndIdIn(userId, notificationDto.getNotificationIds());
        if (notificationEntities == null || notificationEntities.isEmpty()) {
            throw new EmptyValidationException("Notification list is empty");
        }

        notificationEntities.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(OffsetDateTime.now());
        });
        notificationRepository.saveAll(notificationEntities);
    }

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