package git.xlamid.eventcommon.kafka.model.enums;

import lombok.Getter;

@Getter
public enum EventType {
    EVENT_CREATED("Event was created"),
    EVENT_UPDATED("Event was updated"),
    EVENT_STARTED("Event started"),
    EVENT_FINISHED("Event finished"),
    EVENT_CANCELLED("Event was cancelled");

    private final String message;

    EventType(String message) {
        this.message = message;
    }
}