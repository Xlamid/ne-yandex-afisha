package git.xlamid.eventmanagerservice.event.repository;

import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {

    @Query("""
            SELECT e FROM EventEntity e
            LEFT JOIN FETCH e.location
            LEFT JOIN FETCH e.user
            WHERE e.id = :id
            """)
    Optional<EventEntity> findByIdWithLocationIdAndUserId(@Param("id") Long id);

    @Query("""
            SELECT e FROM EventEntity e
            LEFT JOIN FETCH e.location
            LEFT JOIN FETCH e.user
            WHERE e.user.id = :userId
            """)
    List<EventEntity> findAllByUserIdWithLocationIdAndUserId(@Param("userId") Long userId);

    List<EventEntity> findAllByUserId(Long userId);
}