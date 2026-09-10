package git.xlamid.eventnotificatorservice.notification.repository;

import git.xlamid.eventnotificatorservice.notification.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    @Query("""
            SELECT n FROM NotificationEntity n
            JOIN FETCH n.payload
            WHERE n.userId = :userId AND n.isRead = false
            """)
    List<NotificationEntity> findAllNotificationsByUserIdAndIsReadFalse(@Param("userId") Long userId);

    List<NotificationEntity> findAllByUserIdAndIsReadFalseAndIdIn(@Param("userId") Long userId,
                                                                  @Param("ids") List<Long> ids);

    @Query("""
            SELECT n FROM NotificationEntity n
            WHERE n.readAt IS NOT NULL AND n.readAt <= :threshold
            """)
    List<NotificationEntity> findAllOldAndReadNotifications(@Param("threshold") OffsetDateTime threshold);
}