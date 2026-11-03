package git.xlamid.eventmanagerservice.cache.enums;

import lombok.Getter;

@Getter
public enum CacheKey {
    LOCATION("location"),
    LOCATIONS("locations"),
    EVENT("event");

    private final String name;
    
    CacheKey(String name) {
        this.name = name;
    }
}