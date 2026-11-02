package git.xlamid.eventnotificatorservice.notification.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheNotificationCounter {
    
    private static final String NOTIF_KEY_PREFIX = "notif:unread:";
    
    private final StringRedisTemplate stringRedisTemplate;
    
    public void incrUnread(Long userId, long incr) {
        try {
            String key = buildKey(userId);
            Long newCount = stringRedisTemplate.opsForValue()
                    .increment(key, incr);
            log.info("unread notifications count for userId={} is {}", userId, newCount);
        } catch (RedisConnectionFailureException e) {
            log.error("redis INCR error for userId '{}'. Redis is unavailable", userId, e);
        }
    }
    
    public void setActualUnread(Long userId, long count) {
        try {
            String key = buildKey(userId);
            stringRedisTemplate.opsForValue()
                    .set(key, String.valueOf(count));
            log.info("set actual unread notifications count for userId={} is {}", userId, count);
        } catch (Exception e) {
            log.error("redis SET error for userId '{}'. Fallback to DB", userId, e);
        }
    }

    private String buildKey(Long userId) {
        return NOTIF_KEY_PREFIX + userId;
    }
}