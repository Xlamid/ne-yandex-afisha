package git.xlamid.eventnotificatorservice.notification.util;

import git.xlamid.eventnotificatorservice.notification.util.enums.UnreadAction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import static git.xlamid.eventnotificatorservice.notification.util.enums.UnreadAction.INCR;
import static git.xlamid.eventnotificatorservice.notification.util.enums.UnreadAction.SET;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheNotificationCounter {
    
    private static final String NOTIF_KEY_PREFIX = "notif:unread:";
    
    private final StringRedisTemplate stringRedisTemplate;
    
    public void incrUnread(Long userId, long incr) {
        actionUnread(INCR, userId, () -> {
            String key = buildKey(userId);
            Long newCount = stringRedisTemplate.opsForValue()
                    .increment(key, incr);
            log.info("unread notifications count for userId={} is {}", userId, newCount);
        });
    }
    
    public void setActualUnread(Long userId, long count) {
        actionUnread(SET, userId, () -> {
            String key = buildKey(userId);
            stringRedisTemplate.opsForValue()
                    .set(key, String.valueOf(count));
            log.info("set actual unread notifications count for userId={} is {}", userId, count);
        });
    }
    
    private void actionUnread(UnreadAction actionName, Long userId, Runnable action) {
        try {
            action.run();
        } catch (Exception e) {
            log.error("redis {} error for userId '{}'. Fallback to DB", actionName, userId, e);
        }
    }

    private String buildKey(Long userId) {
        return NOTIF_KEY_PREFIX + userId;
    }
}