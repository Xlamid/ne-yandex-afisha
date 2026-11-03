package git.xlamid.eventmanagerservice.controller;

import git.xlamid.eventmanagerservice.EventManagerAbstractWithContainerTest;
import git.xlamid.eventmanagerservice.event.repository.EventRepository;
import git.xlamid.eventmanagerservice.location.dto.CreateLocationDto;
import git.xlamid.eventmanagerservice.location.dto.UpdateLocationDto;
import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import git.xlamid.eventmanagerservice.location.repository.LocationRepository;
import git.xlamid.eventmanagerservice.registration.repository.RegistrationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LocationControllerCacheUnavailableIntegrationTest extends EventManagerAbstractWithContainerTest {

    private static final String BASE_URL = "/locations";
    private static final String ADMIN_LOGIN = "admin1";
    private static final String USER_LOGIN = "user1";
    private static final String CACHE_MODE_PARAM = "cacheMode";
    private static final String SPRING_MODE = "SPRING";

    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private RegistrationRepository registrationRepository;

    @DynamicPropertySource
    static void overrideRedisPort(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.port", () -> 65534);
    }

    @BeforeEach
    public void setupAll() {
        registrationRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
    }

    @AfterEach
    public void cleanup() {
        registrationRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        locationRepository.deleteAllInBatch();
    }

    @Test
    void shouldReturnLocationFromDbWhenRedisUnavailableForGetLocationById() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Hall 1"));
    }

    @Test
    void shouldReturnLocationsFromDbWhenRedisUnavailableForGetLocations() throws Exception {
        // Arrange
        saveLocation("Hall 1", 100);
        saveLocation("Hall 2", 200);

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldCreateLocationInDbWhenRedisUnavailableForCreateLocation() throws Exception {
        // Arrange
        CreateLocationDto createDto = new CreateLocationDto("Hall New", "Address New", 150, "Desc New");

        // Act
        mockMvc.perform(post(BASE_URL)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(ADMIN_LOGIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Hall New"));

        // Verify
        assertEquals(1, locationRepository.findAll().size());
        assertEquals("Hall New", locationRepository.findAll().getFirst().getName());
    }

    @Test
    void shouldUpdateLocationInDbWhenRedisUnavailableForUpdateLocation() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall Old", 100);
        UpdateLocationDto updateDto = new UpdateLocationDto("Hall Updated", "Address Updated", 120, "Desc Updated");

        // Act
        mockMvc.perform(put(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(ADMIN_LOGIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hall Updated"));

        // Verify
        LocationEntity updated = locationRepository.findById(saved.getId()).orElseThrow();
        assertEquals("Hall Updated", updated.getName());
    }

    @Test
    void shouldDeleteLocationFromDbWhenRedisUnavailableForDeleteLocation() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(ADMIN_LOGIN)))
                .andDo(print())
                // Assert
                .andExpect(status().isNoContent());

        // Verify
        assertTrue(locationRepository.findById(saved.getId()).isEmpty());
    }

    private LocationEntity saveLocation(String name, int capacity) {
        return locationRepository.save(new LocationEntity(
                null,
                name,
                "Address of " + name,
                capacity,
                "Desc of " + name,
                null
        ));
    }
}