package git.xlamid.eventnotificatorservice.notification.util;

import git.xlamid.eventcommon.kafka.model.FieldChange;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class NotificationChangesConverter {

    public Map<String, Object> convertChangesToPayload(List<FieldChange> changes) {
        if (changes == null || changes.isEmpty()) {
            return new HashMap<>();
        }
        return changes.stream()
                .map(change -> {
                    Map<String, Object> diff = new HashMap<>();
                    diff.put("oldValue", change.getOldValue());
                    diff.put("newValue", change.getNewValue());
                    return Map.entry(change.getField(), diff);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}