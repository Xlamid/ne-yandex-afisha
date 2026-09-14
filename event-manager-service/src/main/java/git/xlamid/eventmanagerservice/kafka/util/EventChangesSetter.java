package git.xlamid.eventmanagerservice.kafka.util;

import git.xlamid.eventcommon.kafka.model.FieldChange;
import git.xlamid.eventmanagerservice.event.entity.EventEntity;

import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Component
public class EventChangesSetter {

    private static final Map<String, Function<EventEntity, Object>> FIELD_EXTRACTORS = Map.of(
            "id", EventEntity::getId,
            "name", EventEntity::getName,
            "date", e -> e.getDate() != null ?
                    e.getDate().withOffsetSameInstant(ZoneOffset.UTC).toString() : null,
            "cost", EventEntity::getCost,
            "duration", EventEntity::getDuration,
            "maxPlaces", EventEntity::getMaxPlaces,
            "occupiedPlaces", EventEntity::getOccupiedPlaces,
            "status", EventEntity::getStatus,
            "locationId", e -> e.getLocation() != null ? e.getLocation().getId() : null,
            "ownerId", e -> e.getOwner() != null ? e.getOwner().getId() : null
    );

    public List<FieldChange> setAndGetChanges(EventEntity oldEventEntity, EventEntity newEventEntity) {
        return FIELD_EXTRACTORS.entrySet().stream()
                .map(entry -> {
                    Object oldValue = oldEventEntity != null ? entry.getValue().apply(oldEventEntity) : null;
                    Object newValue = newEventEntity != null ? entry.getValue().apply(newEventEntity) : null;
                    return !Objects.equals(oldValue, newValue) ?
                            new FieldChange(entry.getKey(), oldValue, newValue) : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}