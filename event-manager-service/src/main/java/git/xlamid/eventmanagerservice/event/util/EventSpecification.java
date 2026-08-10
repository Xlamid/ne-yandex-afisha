package git.xlamid.eventmanagerservice.event.util;

import git.xlamid.eventmanagerservice.event.dto.EventSearchRequestDto;
import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.event.model.enums.EventStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class EventSpecification {

    public Specification<EventEntity> setSpecification(EventSearchRequestDto filterDto) {
        return Specification
                .where(hasName(filterDto.getName()))
                .and(hasPlacesMin(filterDto.getPlacesMin()))
                .and(hasPlacesMax(filterDto.getPlacesMax()))
                .and(hasDateStartAfter(filterDto.getDateStartAfter()))
                .and(hasDateStartBefore(filterDto.getDateStartBefore()))
                .and(hasCostMin(filterDto.getCostMin()))
                .and(hasCostMax(filterDto.getCostMax()))
                .and(hasDurationMin(filterDto.getDurationMin()))
                .and(hasDurationMax(filterDto.getDurationMax()))
                .and(hasLocationId(filterDto.getLocationId()))
                .and(hasEventStatus(filterDto.getEventStatus()));
    }

    private Specification<EventEntity> hasName(String name) {
        return ((root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.equal(root.get("name"), name);
        });
    }

    private Specification<EventEntity> hasPlacesMin(Integer placesMin) {
        return ((root, query, cb) -> {
            if (numberIsNull(placesMin)) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("maxPlaces"), placesMin);
        });
    }

    private Specification<EventEntity> hasPlacesMax(Integer placesMax) {
        return ((root, query, cb) -> {
            if (numberIsNull(placesMax)) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("maxPlaces"), placesMax);
        });
    }

    private Specification<EventEntity> hasDateStartAfter(OffsetDateTime dateStartAfter) {
        return ((root, query, cb) -> {
            if (dateStartAfter == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("date"), dateStartAfter);
        });
    }

    private Specification<EventEntity> hasDateStartBefore(OffsetDateTime dateStartBefore) {
        return ((root, query, cb) -> {
            if (dateStartBefore == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("date"), dateStartBefore);
        });
    }

    private Specification<EventEntity> hasCostMin(Integer minCost) {
        return ((root, query, cb) -> {
            if (numberIsNull(minCost)) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("cost"), minCost);
        });
    }

    private Specification<EventEntity> hasCostMax(Integer maxCost) {
        return ((root, query, cb) -> {
            if (numberIsNull(maxCost)) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("cost"), maxCost);
        });
    }

    private Specification<EventEntity> hasDurationMin(Integer minDuration) {
        return ((root, query, cb) -> {
            if (numberIsNull(minDuration)) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("duration"), minDuration);
        });
    }

    private Specification<EventEntity> hasDurationMax(Integer maxDuration) {
        return ((root, query, cb) -> {
            if (numberIsNull(maxDuration)) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(root.get("duration"), maxDuration);
        });
    }

    private Specification<EventEntity> hasLocationId(Long locationId) {
        return ((root, query, cb) -> {
            if (numberIsNull(locationId)) {
                return cb.conjunction();
            }
            return cb.equal(root.get("location").get("id"), locationId);
        });
    }

    private Specification<EventEntity> hasEventStatus(EventStatus eventStatus) {
        return ((root, query, cb) -> {
            if (eventStatus == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), eventStatus.name());
        });
    }

    private boolean numberIsNull(Number number) {
        return number == null || number.doubleValue() < 0;
    }
}