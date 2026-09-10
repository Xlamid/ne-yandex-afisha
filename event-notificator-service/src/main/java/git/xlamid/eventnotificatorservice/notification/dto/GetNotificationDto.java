package git.xlamid.eventnotificatorservice.notification.dto;

import git.xlamid.eventcommon.kafka.model.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetNotificationDto {

    private Long notificationId;
    private EventType type;
    private Long eventId;
    private OffsetDateTime createdAt;
    private Boolean isRead;
    private String message;
    private Map<String,Object> payload;
}