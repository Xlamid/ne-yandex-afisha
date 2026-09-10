package git.xlamid.eventnotificatorservice.notification.mapper;

import git.xlamid.eventnotificatorservice.notification.dto.GetNotificationDto;
import git.xlamid.eventnotificatorservice.notification.entity.NotificationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "id", target = "notificationId")
    @Mapping(source = "payload.eventType", target = "type")
    @Mapping(source = "payload.eventId", target = "eventId")
    @Mapping(source = "payload.payload", target = "payload")
    @Mapping(source = "isRead", target = "isRead")
    GetNotificationDto entityToGetDto(NotificationEntity entity);
}