package git.xlamid.eventnotificatorservice.cache;

import git.xlamid.eventcommon.kafka.model.enums.EventType;
import git.xlamid.eventnotificatorservice.EventNotificatorAbstractWithContainerTest;
import git.xlamid.eventnotificatorservice.notification.entity.NotificationEntity;
import git.xlamid.eventnotificatorservice.notification.entity.NotificationEventPayloadEntity;
import git.xlamid.eventnotificatorservice.notification.repository.NotificationEventPayloadRepository;
import git.xlamid.eventnotificatorservice.notification.repository.NotificationRepository;
import git.xlamid.eventnotificatorservice.notification.util.CacheNotificationCounter;
import git.xlamid.eventnotificatorservice.util.UserTestUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class CacheNotificationCounterIntegrationTest extends EventNotificatorAbstractWithContainerTest {

    private static final String NOTIF_KEY_PREFIX = "notif:unread:";
    private static final String BASE_URL = "/notifications";
    private static final Long USER_ID = 900001L;
    private static final Long OTHER_USER_ID = 900002L;
    private static final Long CONTROLLER_USER_ID = 2L;
    private static final Long CONTROLLER_OTHER_USER_ID = 3L;

    @Autowired
    private CacheNotificationCounter cacheNotificationCounter;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationEventPayloadRepository payloadRepository;

    @BeforeEach
    public void setup() {
        stringRedisTemplate.delete(NOTIF_KEY_PREFIX + USER_ID);
        stringRedisTemplate.delete(NOTIF_KEY_PREFIX + OTHER_USER_ID);
        stringRedisTemplate.delete(NOTIF_KEY_PREFIX + Long.MAX_VALUE);
        stringRedisTemplate.delete(NOTIF_KEY_PREFIX + CONTROLLER_USER_ID);
        stringRedisTemplate.delete(NOTIF_KEY_PREFIX + CONTROLLER_OTHER_USER_ID);
    }

    // Correct cases

    @Test
    void shouldCreateCounterOnFirstIncrement() {
        // Act
        cacheNotificationCounter.incrUnread(USER_ID, 1);

        // Assert
        assertEquals("1", getValue(USER_ID));
    }

    @Test
    void shouldAccumulateSeveralIncrements() {
        // Act
        cacheNotificationCounter.incrUnread(USER_ID, 1);
        cacheNotificationCounter.incrUnread(USER_ID, 4);
        cacheNotificationCounter.incrUnread(USER_ID, 5);

        // Assert
        assertEquals("10", getValue(USER_ID));
    }

    @Test
    void shouldSetActualUnreadWhenKeyAbsent() {
        // Act
        cacheNotificationCounter.setActualUnread(USER_ID, 7);

        // Assert
        assertEquals("7", getValue(USER_ID));
    }

    @Test
    void shouldOverwriteExistingCounterOnSetActualUnread() {
        // Arrange
        cacheNotificationCounter.incrUnread(USER_ID, 15);

        // Act
        cacheNotificationCounter.setActualUnread(USER_ID, 3);

        // Assert
        assertEquals("3", getValue(USER_ID));
    }

    @Test
    void shouldKeepCountersOfDifferentUsersIndependent() {
        // Act
        cacheNotificationCounter.incrUnread(USER_ID, 3);
        cacheNotificationCounter.setActualUnread(OTHER_USER_ID, 8);

        // Assert
        assertEquals("3", getValue(USER_ID));
        assertEquals("8", getValue(OTHER_USER_ID));
    }

    // Edge cases

    @Test
    void shouldCreateZeroCounterWhenIncrementByZero() {
        // Act
        cacheNotificationCounter.incrUnread(USER_ID, 0);

        // Assert
        assertEquals("0", getValue(USER_ID));
    }

    @Test
    void shouldKeepValueWhenIncrementByZeroOnExistingCounter() {
        // Arrange
        cacheNotificationCounter.setActualUnread(USER_ID, 4);

        // Act
        cacheNotificationCounter.incrUnread(USER_ID, 0);

        // Assert
        assertEquals("4", getValue(USER_ID));
    }

    @Test
    void shouldSetZeroActualUnread() {
        // Arrange
        cacheNotificationCounter.setActualUnread(USER_ID, 6);

        // Act
        cacheNotificationCounter.setActualUnread(USER_ID, 0);

        // Assert
        assertEquals("0", getValue(USER_ID));
    }

    @Test
    void shouldSetLargeActualUnreadValue() {
        // Act
        cacheNotificationCounter.setActualUnread(USER_ID, Long.MAX_VALUE);

        // Assert
        assertEquals(String.valueOf(Long.MAX_VALUE), getValue(USER_ID));
    }

    // Failure cases

    @Test
    void shouldFailIncrementWhenStoredValueIsNotNumber() {
        // Arrange
        stringRedisTemplate.opsForValue().set(NOTIF_KEY_PREFIX + USER_ID, "not-a-number");

        // Act & Assert
        assertThrows(DataAccessException.class, () -> cacheNotificationCounter.incrUnread(USER_ID, 1));
        assertEquals("not-a-number", getValue(USER_ID));
    }

    @Test
    void shouldFailIncrementOnOverflow() {
        // Arrange
        cacheNotificationCounter.setActualUnread(USER_ID, Long.MAX_VALUE);

        // Act & Assert
        assertThrows(DataAccessException.class, () -> cacheNotificationCounter.incrUnread(USER_ID, 1));
        assertEquals(String.valueOf(Long.MAX_VALUE), getValue(USER_ID));
    }

    @Test
    void shouldNotThrowWhenRedisConnectionFailsOnSetActualUnread() {
        // Arrange
        StringRedisTemplate mockTemplate = mock(StringRedisTemplate.class);
        when(mockTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Unable to connect to Redis"));
        CacheNotificationCounter counter = new CacheNotificationCounter(mockTemplate);

        // Act & Assert
        assertDoesNotThrow(() -> counter.setActualUnread(USER_ID, 5));
    }

    @Test
    void shouldNotThrowWhenRedisConnectionFailsOnIncrUnread() {
        // Arrange
        StringRedisTemplate mockTemplate = mock(StringRedisTemplate.class);
        when(mockTemplate.opsForValue()).thenThrow(new RedisConnectionFailureException("Unable to connect to Redis"));
        CacheNotificationCounter counter = new CacheNotificationCounter(mockTemplate);

        // Act & Assert
        assertDoesNotThrow(() -> counter.incrUnread(USER_ID, 1));
    }
    
    @Test
    void shouldSetCounterToRemainingUnreadWhenPartOfNotificationsMarkedAsRead() throws Exception {
        // Arrange
        NotificationEntity n1 = createTestNotification(CONTROLLER_USER_ID, false);
        NotificationEntity n2 = createTestNotification(CONTROLLER_USER_ID, false);
        createTestNotification(CONTROLLER_USER_ID, false);
        stringRedisTemplate.opsForValue().set(NOTIF_KEY_PREFIX + CONTROLLER_USER_ID, "100");

        // Act
        markAsRead(n1.getId(), n2.getId())
                .andExpect(status().isNoContent());

        // Assert
        assertEquals("1", getValue(CONTROLLER_USER_ID));
    }

    @Test
    void shouldSetCounterToZeroWhenAllNotificationsMarkedAsRead() throws Exception {
        // Arrange
        NotificationEntity n1 = createTestNotification(CONTROLLER_USER_ID, false);
        NotificationEntity n2 = createTestNotification(CONTROLLER_USER_ID, false);

        // Act
        markAsRead(n1.getId(), n2.getId())
                .andExpect(status().isNoContent());

        // Assert
        assertEquals("0", getValue(CONTROLLER_USER_ID));
    }

    @Test
    void shouldCountOnlyOwnUnreadAndKeepOtherUserCounterWhenRequestContainsForeignNotification() throws Exception {
        // Arrange
        NotificationEntity own = createTestNotification(CONTROLLER_USER_ID, false);
        createTestNotification(CONTROLLER_USER_ID, false);
        createTestNotification(CONTROLLER_USER_ID, true);
        NotificationEntity foreign = createTestNotification(CONTROLLER_OTHER_USER_ID, false);
        stringRedisTemplate.opsForValue().set(NOTIF_KEY_PREFIX + CONTROLLER_OTHER_USER_ID, "5");

        // Act
        markAsRead(own.getId(), foreign.getId())
                .andExpect(status().isNoContent());

        // Assert
        assertEquals("1", getValue(CONTROLLER_USER_ID));
        assertEquals("5", getValue(CONTROLLER_OTHER_USER_ID));
    }

    @Test
    void shouldNotChangeCounterWhenNotificationIdsEmpty() throws Exception {
        // Arrange
        createTestNotification(CONTROLLER_USER_ID, false);
        stringRedisTemplate.opsForValue().set(NOTIF_KEY_PREFIX + CONTROLLER_USER_ID, "1");

        // Act
        markAsRead()
                .andExpect(status().isBadRequest());

        // Assert
        assertEquals("1", getValue(CONTROLLER_USER_ID));
    }

    @Test
    void shouldNotChangeCounterWhenOnlyForeignOrAlreadyReadNotificationsRequested() throws Exception {
        // Arrange
        NotificationEntity foreign = createTestNotification(CONTROLLER_OTHER_USER_ID, false);
        NotificationEntity alreadyRead = createTestNotification(CONTROLLER_USER_ID, true);
        createTestNotification(CONTROLLER_USER_ID, false);
        stringRedisTemplate.opsForValue().set(NOTIF_KEY_PREFIX + CONTROLLER_USER_ID, "7");

        // Act
        markAsRead(foreign.getId(), alreadyRead.getId())
                .andExpect(status().isBadRequest());

        // Assert
        assertEquals("7", getValue(CONTROLLER_USER_ID));
        assertNull(getValue(CONTROLLER_OTHER_USER_ID));
    }

    @Test
    void shouldNotChangeCounterWhenNoToken() throws Exception {
        // Arrange
        NotificationEntity n1 = createTestNotification(CONTROLLER_USER_ID, false);
        stringRedisTemplate.opsForValue().set(NOTIF_KEY_PREFIX + CONTROLLER_USER_ID, "1");
        Map<String, Object> requestDto = Map.of("notificationIds", List.of(n1.getId()));

        // Act
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        // Assert
        assertEquals("1", getValue(CONTROLLER_USER_ID));
    }

    private ResultActions markAsRead(Long... ids) throws Exception {
        Map<String, Object> requestDto = Map.of("notificationIds", List.of(ids));
        return mockMvc.perform(post(BASE_URL)
                        .header("Authorization", getAuthHeader(UserTestUtil.USER_LOGIN_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print());
    }

    private NotificationEntity createTestNotification(Long userId, boolean isRead) {
        NotificationEventPayloadEntity payloadEntity = new NotificationEventPayloadEntity(
                null,
                UUID.randomUUID(),
                1L,
                EventType.EVENT_UPDATED.name(),
                OffsetDateTime.now(),
                null,
                null,
                null,
                null
        );
        NotificationEntity notificationEntity = new NotificationEntity(
                null,
                userId,
                isRead,
                OffsetDateTime.now(),
                null,
                payloadEntity
        );
        if (isRead) {
            notificationEntity.setReadAt(OffsetDateTime.now());
        }

        payloadRepository.save(payloadEntity);
        return notificationRepository.save(notificationEntity);
    }

    private String getValue(Long userId) {
        return stringRedisTemplate.opsForValue().get(NOTIF_KEY_PREFIX + userId);
    }
}