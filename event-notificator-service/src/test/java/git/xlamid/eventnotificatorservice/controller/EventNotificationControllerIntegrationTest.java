package git.xlamid.eventnotificatorservice.controller;

import git.xlamid.eventcommon.kafka.model.enums.EventType;
import git.xlamid.eventnotificatorservice.EventNotificatorAbstractWithContainerTest;
import git.xlamid.eventnotificatorservice.notification.entity.NotificationEntity;
import git.xlamid.eventnotificatorservice.notification.entity.NotificationEventPayloadEntity;
import git.xlamid.eventnotificatorservice.notification.repository.NotificationEventPayloadRepository;
import git.xlamid.eventnotificatorservice.notification.repository.NotificationRepository;
import git.xlamid.eventnotificatorservice.util.UserTestUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class EventNotificationControllerIntegrationTest extends EventNotificatorAbstractWithContainerTest {

    private static final Long USER_ID_1 = 2L;
    private static final Long USER_ID_2 = 3L;

    private static final String BASE_URL = "/notifications";

    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationEventPayloadRepository payloadRepository;

    // getUnreadNotifications()

    @Test
    void shouldReturnUnreadNotificationsForUserForGetUnreadNotifications() throws Exception {
        // Arrange
        NotificationEntity unread1 = createTestNotification(USER_ID_1, false, EventType.EVENT_UPDATED);
        NotificationEntity unread2 = createTestNotification(USER_ID_1, false, EventType.EVENT_CANCELLED);
        createTestNotification(USER_ID_1, true, EventType.EVENT_UPDATED);
        createTestNotification(USER_ID_2, false, EventType.EVENT_CREATED);

        // Act
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", getAuthHeader(UserTestUtil.USER_LOGIN_1)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(USER_ID_1))
                .andExpect(jsonPath("$[0].notificationId").value(unread1.getId()))
                .andExpect(jsonPath("$[0].isRead").value(unread1.getIsRead()))
                .andExpect(jsonPath("$[0].message").value(EventType.EVENT_UPDATED.getMessage()))
                .andExpect(jsonPath("$[0].type").value(EventType.EVENT_UPDATED.name()))
                .andExpect(jsonPath("$[1].notificationId").value(unread2.getId()));
    }

    @Test
    void shouldReturnEmptyListWhenNoUnreadNotificationsForGetUnreadNotifications() throws Exception {
        // Arrange
        createTestNotification(USER_ID_1, true, EventType.EVENT_UPDATED);

        // Act
        mockMvc.perform(get(BASE_URL)
                        .header("Authorization", getAuthHeader(UserTestUtil.USER_LOGIN_1)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnUnauthorizedWhenNoTokenForGetUnreadNotifications() throws Exception {
        // Act & Assert
        mockMvc.perform(get(BASE_URL))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    // markNotificationsAsRead()

    @Test
    void shouldMarkNotificationsAsReadAndReturnNoContentForMarkNotificationsAsRead() throws Exception {
        // Arrange
        NotificationEntity notification1 =
                createTestNotification(USER_ID_1, false, EventType.EVENT_UPDATED);
        NotificationEntity notification2 =
                createTestNotification(USER_ID_1, false, EventType.EVENT_CREATED);
        NotificationEntity notification3 =
                createTestNotification(USER_ID_1, false, EventType.EVENT_CANCELLED);

        Map<String, Object> requestDto = Map.of(
                "notificationIds", List.of(notification1.getId(), notification2.getId())
        );

        // Act
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", getAuthHeader(UserTestUtil.USER_LOGIN_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isNoContent());

        // Verify
        NotificationEntity updatedNotification1 = notificationRepository
                .findById(notification1.getId()).orElseThrow();
        NotificationEntity updatedNotification2 = notificationRepository
                .findById(notification2.getId()).orElseThrow();
        NotificationEntity untouchedNotification3 = notificationRepository
                .findById(notification3.getId()).orElseThrow();

        assertTrue(updatedNotification1.getIsRead());
        assertNotNull(updatedNotification1.getReadAt());
        assertTrue(updatedNotification2.getIsRead());
        assertNotNull(updatedNotification2.getReadAt());

        assertFalse(untouchedNotification3.getIsRead());
        assertNull(untouchedNotification3.getReadAt());
    }

    @Test
    void shouldIgnoreOtherUsersNotificationsForMarkNotificationsAsRead() throws Exception {
        // Arrange
        NotificationEntity user1Notification =
                createTestNotification(USER_ID_1, false, EventType.EVENT_UPDATED);
        NotificationEntity user2Notification =
                createTestNotification(USER_ID_2, false, EventType.EVENT_CREATED);

        Map<String, Object> requestDto = Map.of(
                "notificationIds", List.of(user1Notification.getId(), user2Notification.getId())
        );

        // Act
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", getAuthHeader(UserTestUtil.USER_LOGIN_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isNoContent());

        // Verify
        NotificationEntity updatedUser1Notification = notificationRepository
                .findById(user1Notification.getId()).orElseThrow();
        NotificationEntity untouchedUser2Notification = notificationRepository
                .findById(user2Notification.getId()).orElseThrow();

        assertTrue(updatedUser1Notification.getIsRead());
        assertFalse(untouchedUser2Notification.getIsRead());
    }

    @Test
    void shouldReturnBadRequestWhenNotificationIdsEmptyForMarkNotificationsAsRead() throws Exception {
        // Arrange
        Map<String, Object> emptyRequestDto = Map.of(
                "notificationIds", List.of()
        );

        // Act
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", getAuthHeader(UserTestUtil.USER_LOGIN_1))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyRequestDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedWhenNoTokenForMarkNotificationsAsRead() throws Exception {
        // Arrange
        Map<String, Object> requestDto = Map.of("notificationIds", List.of(1L));

        // Act
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isUnauthorized());
    }

    // helpers

    private NotificationEntity createTestNotification(Long userId, boolean isRead, EventType eventType) {
        NotificationEventPayloadEntity payloadEntity = new NotificationEventPayloadEntity(
                null,
                UUID.randomUUID(),
                1L,
                eventType.name(),
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
}