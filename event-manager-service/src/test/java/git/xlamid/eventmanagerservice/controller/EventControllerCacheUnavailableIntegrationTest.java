package git.xlamid.eventmanagerservice.controller;

import git.xlamid.eventmanagerservice.EventManagerAbstractWithContainerTest;
import git.xlamid.eventmanagerservice.event.dto.UpdateEventDto;
import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.event.model.enums.EventStatus;
import git.xlamid.eventmanagerservice.event.repository.EventRepository;
import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import git.xlamid.eventmanagerservice.registration.repository.RegistrationRepository;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import git.xlamid.eventmanagerservice.util.DataTestUtil;
import git.xlamid.eventmanagerservice.util.EventCreator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.time.OffsetDateTime;

import static git.xlamid.eventmanagerservice.event.model.enums.EventStatus.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventControllerCacheUnavailableIntegrationTest extends EventManagerAbstractWithContainerTest {

    private static final String BASE_URL = "/events";
    private static final String CACHE_MODE_PARAM = "cacheMode";
    private static final String SPRING_MODE = "SPRING";

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private RegistrationRepository registrationRepository;
    @Autowired
    private DataTestUtil dataTestUtil;
    @Autowired
    private EventCreator eventCreator;

    private UserEntity testUser1;
    private LocationEntity testLocation;

    @DynamicPropertySource
    static void overrideRedisPort(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.port", () -> 65534);
    }

    @BeforeEach
    public void setupAll() {
        registrationRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        testUser1 = userTestUtil.getTestUserByLogin("user1");
        testLocation = dataTestUtil.getTestLocation();
    }

    @AfterEach
    public void cleanup() {
        registrationRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
    }

    @Test
    void shouldReturnEventFromDbWhenRedisUnavailableForGetEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Event 1"))
                .andExpect(jsonPath("$.status").value(WAIT_START.name()));
    }

    @Test
    void shouldUpdateEventInDbWhenRedisUnavailableForUpdateEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        UpdateEventDto updateDto = new UpdateEventDto(
                "Event Updated",
                OffsetDateTime.now().plusDays(5),
                200,
                90,
                100,
                testLocation.getId()
        );

        // Act
        mockMvc.perform(put(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Event Updated"));

        // Verify
        EventEntity updated = eventRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Event Updated", updated.getName());
    }

    @Test
    void shouldDeleteEventFromDbWhenRedisUnavailableForDeleteEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isNoContent());

        // Verify
        assertEquals(CANCELLED.name(), eventRepository.findById(saved.getId()).orElseThrow().getStatus());
    }

    private EventEntity saveEvent(String name, EventStatus status) {
        return eventRepository.save(
                eventCreator.createEventEntity(name, testUser1, status, testLocation)
        );
    }
}