package git.xlamid.eventnotificatorservice.notification.mapper;

import git.xlamid.eventcommon.kafka.model.NotificationKafkaEvent;
import git.xlamid.eventnotificatorservice.notification.entity.NotificationEventPayloadEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationEventPayloadMapper {

    NotificationEventPayloadEntity kafkaEventToEntity(NotificationKafkaEvent kafkaEvent);
}