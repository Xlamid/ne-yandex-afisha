package git.xlamid.eventmanagerservice.controller;

import git.xlamid.eventmanagerservice.EventManagerAbstractWithContainerTest;
import git.xlamid.eventmanagerservice.event.dto.CreateEventDto;
import git.xlamid.eventmanagerservice.event.dto.GetEventDto;
import git.xlamid.eventmanagerservice.event.dto.UpdateEventDto;
import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.event.model.enums.EventStatus;
import git.xlamid.eventmanagerservice.event.repository.EventRepository;
import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import git.xlamid.eventmanagerservice.location.repository.LocationRepository;
import git.xlamid.eventmanagerservice.registration.repository.RegistrationRepository;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import git.xlamid.eventmanagerservice.util.DataTestUtil;
import git.xlamid.eventmanagerservice.util.EventCreator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;
import java.util.Objects;

import static git.xlamid.eventmanagerservice.event.model.enums.EventStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventControllerForSpringCacheIntegrationTest extends EventManagerAbstractWithContainerTest {

    private static final String BASE_URL = "/events";
    private static final String CACHE_MODE_PARAM = "cacheMode";
    private static final String SPRING_MODE = "SPRING";
    private static final String DB_MODE = "DB";
    private static final String EVENT_CACHE = "event";

    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private RegistrationRepository registrationRepository;
    @Autowired
    private DataTestUtil dataTestUtil;
    @Autowired
    private EventCreator eventCreator;

    private UserEntity testUser1;
    private UserEntity testUser2;
    private LocationEntity testLocation;

    @BeforeEach
    public void setupAll() {
        clearEventsAndCache();
        testUser1 = userTestUtil.getTestUserByLogin("user1");
        testUser2 = userTestUtil.getTestUserByLogin("user2");
        testLocation = dataTestUtil.getTestLocation();
    }

    @AfterEach
    public void cleanup() {
        clearEventsAndCache();
    }

    // getEventById()

    @Test
    void shouldPutEventInCacheForGetEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        assertNull(getCache().get(saved.getId()));

        // Act
        mockMvc.perform(get(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Event 1"))
                .andExpect(jsonPath("$.status").value(WAIT_START.name()));

        // Verify
        assertCached(saved.getId());
        GetEventDto cached = (GetEventDto) Objects.requireNonNull(getCache().get(saved.getId())).get();
        assertNotNull(cached);
        assertEquals(saved.getId(), cached.getId());
        assertEquals("Event 1", cached.getName());
        assertEquals(WAIT_START, cached.getStatus());
    }

    @Test
    void shouldNotPutEventInCacheWhenCacheModeIsDbForGetEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);

        // Act
        mockMvc.perform(get(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, DB_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isOk());

        // Verify
        assertNull(getCache().get(saved.getId()));
    }

    @Test
    void shouldReturnCachedEventWhenChangedInDbDirectlyForGetEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        performGetEventById(saved.getId());
        assertCached(saved.getId());
        saved.setName("Changed In Db");
        eventRepository.save(saved);

        // Act
        mockMvc.perform(get(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Event 1"));
    }

    @Test
    void shouldReturnNotFoundAndNotPutInCacheWhenNotExistsForGetEventById() throws Exception {
        // Act
        mockMvc.perform(get(BASE_URL + "/999")
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isNotFound());

        // Verify
        assertNull(getCache().get(999L));
    }

    @Test
    void shouldReturnUnauthorizedAndNotPutInCacheWhenNotAuthenticatedForGetEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);

        // Act
        mockMvc.perform(get(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE))
                .andDo(print())
                // Assert
                .andExpect(status().isUnauthorized());

        // Verify
        assertNull(getCache().get(saved.getId()));
    }

    // createEvent()

    @Test
    void shouldNotPutEventInCacheForCreateEvent() throws Exception {
        // Arrange
        CreateEventDto createDto = new CreateEventDto(
                "Summer Festival",
                OffsetDateTime.now().plusDays(5),
                500,
                120,
                100,
                testLocation.getId()
        );

        // Act
        mockMvc.perform(post(BASE_URL)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());

        // Verify
        Long createdId = eventRepository.findAll().getFirst().getId();
        assertNull(getCache().get(createdId));
        assertEquals(1, eventRepository.findAll().size());
    }

    // updateEventById()

    @Test
    void shouldEvictEventFromCacheForUpdateEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        performGetEventById(saved.getId());
        assertCached(saved.getId());
        UpdateEventDto updateDto = createUpdateDto("Event Updated", 100);

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
        assertEvicted(saved.getId());
        mockMvc.perform(get(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Event Updated"));
        assertCached(saved.getId());
    }

    @Test
    void shouldNotEvictOtherEventCacheForUpdateEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        EventEntity other = saveEvent("Event 2", WAIT_START);
        performGetEventById(other.getId());
        assertCached(other.getId());

        // Act
        mockMvc.perform(put(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUpdateDto("Event Updated", 100))))
                .andDo(print())
                // Assert
                .andExpect(status().isOk());

        // Verify
        assertCached(other.getId());
    }

    @Test
    void shouldNotEvictCacheWhenEventNotFoundForUpdateEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        performGetEventById(saved.getId());
        assertCached(saved.getId());

        // Act
        mockMvc.perform(put(BASE_URL + "/999")
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUpdateDto("Event Updated", 100))))
                .andDo(print())
                // Assert
                .andExpect(status().isNotFound());

        // Verify
        assertCached(saved.getId());
    }

    @Test
    void shouldNotEvictCacheWhenNotOwnerForUpdateEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        performGetEventById(saved.getId());
        assertCached(saved.getId());

        // Act
        mockMvc.perform(put(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser2.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createUpdateDto("Event Updated", 100))))
                .andDo(print())
                // Assert
                .andExpect(status().isForbidden());

        // Verify
        assertCached(saved.getId());
        assertEquals("Event 1", eventRepository.findById(saved.getId()).orElseThrow().getName());
    }

    @Test
    void shouldNotEvictCacheWhenDateIsTooEarlyForUpdateEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        performGetEventById(saved.getId());
        assertCached(saved.getId());
        UpdateEventDto updateDto = createUpdateDto("Event Updated", 100);
        updateDto.setDate(OffsetDateTime.now().plusHours(12));

        // Act
        mockMvc.perform(put(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());

        // Verify
        assertCached(saved.getId());
    }

    @Test
    void shouldNotEvictCacheWhenMaxPlacesExceedsLocationCapacityForUpdateEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        performGetEventById(saved.getId());
        assertCached(saved.getId());
        UpdateEventDto updateDto = createUpdateDto("Event Updated", testLocation.getCapacity() + 1);

        // Act
        mockMvc.perform(put(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());

        // Verify
        assertCached(saved.getId());
    }

    @Test
    void shouldNotEvictCacheWhenDtoIsInvalidForUpdateEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        performGetEventById(saved.getId());
        assertCached(saved.getId());
        UpdateEventDto updateDto = createUpdateDto("Event Updated", 100);
        updateDto.setCost(0);

        // Act
        mockMvc.perform(put(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());

        // Verify
        assertCached(saved.getId());
    }

    @Test
    void shouldEvictEventFromCacheWhenMaxPlacesEqualsLocationCapacityForUpdateEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        performGetEventById(saved.getId());
        assertCached(saved.getId());
        UpdateEventDto updateDto = createUpdateDto("Event Updated", testLocation.getCapacity());

        // Act
        mockMvc.perform(put(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxPlaces").value(testLocation.getCapacity()));

        // Verify
        assertEvicted(saved.getId());
    }

    // deleteEventById()

    @Test
    void shouldEvictEventFromCacheForDeleteEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        performGetEventById(saved.getId());
        assertCached(saved.getId());

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isNoContent());

        // Verify
        assertEvicted(saved.getId());
        mockMvc.perform(get(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(CANCELLED.name()));
    }

    @Test
    void shouldNotEvictOtherEventCacheForDeleteEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        EventEntity other = saveEvent("Event 2", WAIT_START);
        performGetEventById(other.getId());
        assertCached(other.getId());

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isNoContent());

        // Verify
        assertCached(other.getId());
    }

    @Test
    void shouldNotEvictCacheWhenEventNotFoundForDeleteEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        performGetEventById(saved.getId());
        assertCached(saved.getId());

        // Act
        mockMvc.perform(delete(BASE_URL + "/999")
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isNotFound());

        // Verify
        assertCached(saved.getId());
    }

    @Test
    void shouldNotEvictCacheWhenNotOwnerForDeleteEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", WAIT_START);
        performGetEventById(saved.getId());
        assertCached(saved.getId());

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser2.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isForbidden());

        // Verify
        assertCached(saved.getId());
        assertEquals(WAIT_START.name(), eventRepository.findById(saved.getId()).orElseThrow().getStatus());
    }

    @Test
    void shouldNotEvictCacheWhenEventAlreadyStartedForDeleteEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", STARTED);
        performGetEventById(saved.getId());
        assertCached(saved.getId());

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());

        // Verify
        assertCached(saved.getId());
        assertEquals(STARTED.name(), eventRepository.findById(saved.getId()).orElseThrow().getStatus());
    }

    @Test
    void shouldNotEvictCacheWhenEventAlreadyCancelledForDeleteEventById() throws Exception {
        // Arrange
        EventEntity saved = saveEvent("Event 1", CANCELLED);
        performGetEventById(saved.getId());
        assertCached(saved.getId());

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());

        // Verify
        assertCached(saved.getId());
    }

    private void performGetEventById(Long id) throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + id)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andExpect(status().isOk());
    }

    private EventEntity saveEvent(String name, EventStatus status) {
        return eventRepository.save(
                eventCreator.createEventEntity(name, testUser1, status, testLocation)
        );
    }

    private UpdateEventDto createUpdateDto(String name, int maxPlaces) {
        return new UpdateEventDto(
                name,
                OffsetDateTime.now().plusDays(5),
                200,
                90,
                maxPlaces,
                testLocation.getId()
        );
    }

    private void assertCached(Long id) {
        assertNotNull(getCache().get(id));
    }

    private void assertEvicted(Long id) {
        assertNull(getCache().get(id));
    }

    private Cache getCache() {
        return Objects.requireNonNull(cacheManager.getCache(EVENT_CACHE));
    }

    private void clearEventsAndCache() {
        registrationRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        getCache().clear();
    }
}