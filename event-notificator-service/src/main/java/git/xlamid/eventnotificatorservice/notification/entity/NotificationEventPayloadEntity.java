package git.xlamid.eventnotificatorservice.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "notification_event_payloads")
public class NotificationEventPayloadEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "message_id", unique = true, nullable = false)
    private UUID messageId;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "occurred_at")
    private OffsetDateTime occurredAt;

    @Column(name = "changed_by_id")
    private Long changedById;

    @Column(name = "owner_id")
    private Long ownerId;

    @Column(name = "payload")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> payload;

    @OneToMany(mappedBy = "payload", fetch = FetchType.LAZY)
    private List<NotificationEntity> notifications = new ArrayList<>();
}