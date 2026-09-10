package git.xlamid.eventnotificatorservice.notification.repository;

import git.xlamid.eventnotificatorservice.notification.entity.NotificationEventPayloadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationEventPayloadRepository extends JpaRepository<NotificationEventPayloadEntity, Long> {

    boolean existsByMessageId(UUID messageId);
}