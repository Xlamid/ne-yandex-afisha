package git.xlamid.eventmanagerservice.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

@Slf4j
public class EventManagerCacheErrorHandler implements CacheErrorHandler {
    
    @Override
    public void handleCacheGetError(@NonNull RuntimeException exception, Cache cache, @NonNull Object key) {
        log.error("redis GET error for cache '{}', key '{}'. Fallback to DB", cache.getName(), key, exception);
    }

    @Override
    public void handleCachePutError(@NonNull RuntimeException exception, Cache cache, @NonNull Object key, @Nullable Object value) {
        log.error("redis PUT error for cache '{}', key '{}'. Fallback to DB", cache.getName(), key, exception);
    }

    @Override
    public void handleCacheEvictError(@NonNull RuntimeException exception, Cache cache, @NonNull Object key) {
        log.error("redis EVICT error for cache '{}', key '{}'. Fallback to DB", cache.getName(), key, exception);
    }

    @Override
    public void handleCacheClearError(@NonNull RuntimeException exception, Cache cache) {
        log.error("redis CLEAR error for cache '{}'", cache.getName(), exception);
    }
}