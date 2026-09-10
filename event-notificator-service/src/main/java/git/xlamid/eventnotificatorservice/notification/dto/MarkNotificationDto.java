package git.xlamid.eventnotificatorservice.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MarkNotificationDto {

    private List<Long> notificationIds;
}