package git.xlamid.eventcommon.kafka.model;

import git.xlamid.eventcommon.kafka.model.enums.EventType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationKafkaEvent {

    private UUID messageId;
    private EventType eventType;
    private Long eventId;
    private OffsetDateTime occurredAt;
    private Long ownerId;
    private Long changedById;
    private List<Long> subscribers;
    private List<FieldChange> changes;
}