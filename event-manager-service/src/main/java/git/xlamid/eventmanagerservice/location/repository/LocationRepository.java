package git.xlamid.eventmanagerservice.location.repository;

import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    Optional<LocationEntity> findByName(String name);
}