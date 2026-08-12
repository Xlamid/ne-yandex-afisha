package git.xlamid.eventmanagerservice.registration.repository;

import git.xlamid.eventmanagerservice.registration.entity.RegistrationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<RegistrationEntity, Long> {

    Optional<RegistrationEntity> findByUserIdAndEventId(Long userId, Long eventId);
}