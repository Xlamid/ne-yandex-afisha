package git.xlamid.eventnotificatorservice.kafka.listener;

import git.xlamid.eventcommon.kafka.model.NotificationKafkaEvent;
import git.xlamid.eventnotificatorservice.notification.repository.NotificationEventPayloadRepository;
import git.xlamid.eventnotificatorservice.notification.service.NotificationProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationEventPayloadRepository payloadRepository;
    private final NotificationProcessingService notificationProcessingService;

    @KafkaListener(topics = "${spring.kafka.topic.notification}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenEvent(ConsumerRecord<Long, NotificationKafkaEvent> record, Acknowledgment ack) {
        NotificationKafkaEvent event = record.value();
        UUID messageId = event.getMessageId();
        log.info("Receive from kafka topic: {} notification event: {}", record.topic(), event);

        try {
            if (payloadRepository.existsByMessageId(messageId)) {
                log.info("Payload already exists for messageId: {}", messageId);
                ack.acknowledge();
                return;
            }
            notificationProcessingService.processEventForSave(event);
            ack.acknowledge();
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate event found for messageId: {}", messageId);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process event for messageId: {}", messageId, e);
            throw e;
        }
    }
}