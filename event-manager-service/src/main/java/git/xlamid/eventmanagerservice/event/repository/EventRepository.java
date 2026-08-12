package git.xlamid.eventmanagerservice.event.repository;

import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {

    @Query("""
            SELECT e FROM EventEntity e
            LEFT JOIN FETCH e.location
            LEFT JOIN FETCH e.owner
            WHERE e.id = :id
            """)
    Optional<EventEntity> findByIdWithLocationIdAndUserId(@Param("id") Long id);

    @Query("""
            SELECT e FROM EventEntity e
            LEFT JOIN FETCH e.location
            LEFT JOIN FETCH e.owner
            WHERE e.owner.id = :userId
            """)
    List<EventEntity> findAllByUserIdWithLocationIdAndUserId(@Param("userId") Long userId);

    @Query("""
            SELECT e FROM EventEntity e
            WHERE e.status = :status AND e.date <= :currentDate
            """)
    List<EventEntity> findAllForStartedEventsWithStatus(@Param("status") String status,
                                                        @Param("currentDate") OffsetDateTime date);

    @Query(value = """
            SELECT * FROM events e
            WHERE e.status = :status AND
                  (e.start_at + e.duration_minutes * INTERVAL '1 minute') <= :currentDate
            """, nativeQuery = true)
    List<EventEntity> findAllForFinishedEventsWithStatus(@Param("status") String status,
                                                         @Param("currentDate") OffsetDateTime date);

    List<EventEntity> findAllByOwnerId(Long userId);
}