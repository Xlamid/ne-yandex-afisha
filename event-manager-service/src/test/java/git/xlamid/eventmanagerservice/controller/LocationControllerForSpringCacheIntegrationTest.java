package git.xlamid.eventmanagerservice.controller;

import git.xlamid.eventmanagerservice.EventManagerAbstractWithContainerTest;
import git.xlamid.eventmanagerservice.event.repository.EventRepository;
import git.xlamid.eventmanagerservice.location.dto.CreateLocationDto;
import git.xlamid.eventmanagerservice.location.dto.GetLocationDto;
import git.xlamid.eventmanagerservice.location.dto.UpdateLocationDto;
import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import git.xlamid.eventmanagerservice.location.repository.LocationRepository;
import git.xlamid.eventmanagerservice.registration.repository.RegistrationRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LocationControllerForSpringCacheIntegrationTest extends EventManagerAbstractWithContainerTest {

    private static final String BASE_URL = "/locations";
    private static final String ADMIN_LOGIN = "admin1";
    private static final String USER_LOGIN = "user1";
    private static final String CACHE_MODE_PARAM = "cacheMode";
    private static final String SPRING_MODE = "SPRING";
    private static final String DB_MODE = "DB";

    private static final String LOCATION_CACHE = "location";
    private static final String LOCATIONS_CACHE = "locations";
    private static final String ALL_KEY = "all";

    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private RegistrationRepository registrationRepository;

    @BeforeEach
    public void setupAll() {
        registrationRepository.deleteAllInBatch();
        eventRepository.deleteAllInBatch();
        clearLocationsAndCache();
    }

    @AfterEach
    public void cleanup() {
        clearLocationsAndCache();
    }

    // getLocationById()

    @Test
    void shouldPutLocationInCacheForGetLocationById() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);
        assertNull(getCache(LOCATION_CACHE).get(saved.getId()));

        // Act
        mockMvc.perform(get(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Hall 1"));

        // Verify
        assertCached(LOCATION_CACHE, saved.getId());
        GetLocationDto cached = (GetLocationDto) Objects.requireNonNull(
                getCache(LOCATION_CACHE).get(saved.getId())).get();
        assertNotNull(cached);
        assertEquals(saved.getId(), cached.getId());
        assertEquals("Hall 1", cached.getName());
    }

    @Test
    void shouldNotPutLocationInCacheWhenCacheModeIsDbForGetLocationById() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);

        // Act
        mockMvc.perform(get(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, DB_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk());

        // Verify
        assertNull(getCache(LOCATION_CACHE).get(saved.getId()));
    }

    @Test
    void shouldReturnCachedLocationWhenDeletedFromDbForGetLocationById() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);
        performGetLocationById(saved.getId());
        assertCached(LOCATION_CACHE, saved.getId());
        locationRepository.deleteById(saved.getId());

        // Act
        mockMvc.perform(get(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(saved.getId()))
                .andExpect(jsonPath("$.name").value("Hall 1"));
    }

    @Test
    void shouldReturnNotFoundAndNotPutInCacheWhenNotExistsForGetLocationById() throws Exception {
        // Act
        mockMvc.perform(get(BASE_URL + "/999")
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andDo(print())
                // Assert
                .andExpect(status().isNotFound());

        // Verify
        assertNull(getCache(LOCATION_CACHE).get(999L));
    }

    @Test
    void shouldReturnUnauthorizedAndNotPutInCacheWhenNotAuthenticatedForGetLocationById() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);

        // Act
        mockMvc.perform(get(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE))
                .andDo(print())
                // Assert
                .andExpect(status().isUnauthorized());

        // Verify
        assertNull(getCache(LOCATION_CACHE).get(saved.getId()));
    }

    // getLocations()

    @Test
    void shouldPutLocationsInCacheForGetLocations() throws Exception {
        // Arrange
        saveLocation("Hall 1", 100);
        saveLocation("Hall 2", 200);
        assertNull(getCache(LOCATIONS_CACHE).get(ALL_KEY));

        // Act
        mockMvc.perform(get(BASE_URL)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        // Verify
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        List<?> cached = (List<?>) Objects.requireNonNull(getCache(LOCATIONS_CACHE).get(ALL_KEY)).get();
        assertNotNull(cached);
        assertEquals(2, cached.size());
    }

    @Test
    void shouldNotPutLocationsInCacheWhenCacheModeIsDbForGetLocations() throws Exception {
        // Arrange
        saveLocation("Hall 1", 100);

        // Act
        mockMvc.perform(get(BASE_URL)
                        .param(CACHE_MODE_PARAM, DB_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        // Verify
        assertNull(getCache(LOCATIONS_CACHE).get(ALL_KEY));
    }

    @Test
    void shouldPutEmptyListInCacheWhenNoLocationsForGetLocations() throws Exception {
        // Act
        mockMvc.perform(get(BASE_URL)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        // Verify
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        List<?> cached = (List<?>) Objects.requireNonNull(getCache(LOCATIONS_CACHE).get(ALL_KEY)).get();
        assertNotNull(cached);
        assertTrue(cached.isEmpty());
    }

    @Test
    void shouldReturnCachedLocationsWhenDbChangedDirectlyForGetLocations() throws Exception {
        // Arrange
        saveLocation("Hall 1", 100);
        performGetLocations();
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        saveLocation("Hall 2", 200);

        // Act
        mockMvc.perform(get(BASE_URL)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Hall 1"));
    }

    // createLocation()

    @Test
    void shouldEvictAllLocationsCacheForCreateLocation() throws Exception {
        // Arrange
        saveLocation("Hall 1", 100);
        performGetLocations();
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        CreateLocationDto createDto = new CreateLocationDto("Hall 2", "Address 2", 200, "Desc 2");

        // Act
        mockMvc.perform(post(BASE_URL)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(ADMIN_LOGIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Hall 2"));

        // Verify
        assertEvicted(LOCATIONS_CACHE, ALL_KEY);
        mockMvc.perform(get(BASE_URL)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldNotEvictAllLocationsCacheWhenNameIsDuplicatedForCreateLocation() throws Exception {
        // Arrange
        saveLocation("Hall 1", 100);
        performGetLocations();
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        CreateLocationDto createDto = new CreateLocationDto("Hall 1", "Address 2", 200, "Desc 2");

        // Act
        mockMvc.perform(post(BASE_URL)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(ADMIN_LOGIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());

        // Verify
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        assertEquals(1, locationRepository.findAll().size());
    }

    @Test
    void shouldNotEvictAllLocationsCacheWhenDtoIsInvalidForCreateLocation() throws Exception {
        // Arrange
        saveLocation("Hall 1", 100);
        performGetLocations();
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        CreateLocationDto createDto = new CreateLocationDto("Hall 2", "Address 2", 4, "Desc 2");

        // Act
        mockMvc.perform(post(BASE_URL)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(ADMIN_LOGIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());

        // Verify
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        assertEquals(1, locationRepository.findAll().size());
    }

    @Test
    void shouldNotEvictAllLocationsCacheWhenUserRoleForCreateLocation() throws Exception {
        // Arrange
        saveLocation("Hall 1", 100);
        performGetLocations();
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        CreateLocationDto createDto = new CreateLocationDto("Hall 2", "Address 2", 200, "Desc 2");

        // Act
        mockMvc.perform(post(BASE_URL)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isForbidden());

        // Verify
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        assertEquals(1, locationRepository.findAll().size());
    }

    // updateLocation()

    @Test
    void shouldEvictLocationAndAllLocationsCacheForUpdateLocation() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);
        performGetLocationById(saved.getId());
        performGetLocations();
        assertCached(LOCATION_CACHE, saved.getId());
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        UpdateLocationDto updateDto = new UpdateLocationDto("Hall Updated", "New Address", 300, "New Desc");

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
        assertEvicted(LOCATION_CACHE, saved.getId());
        assertEvicted(LOCATIONS_CACHE, ALL_KEY);
        mockMvc.perform(get(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Hall Updated"));
        mockMvc.perform(get(BASE_URL)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Hall Updated"));
    }

    @Test
    void shouldNotEvictOtherLocationCacheForUpdateLocation() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);
        LocationEntity other = saveLocation("Hall 2", 200);
        performGetLocationById(other.getId());
        assertCached(LOCATION_CACHE, other.getId());
        UpdateLocationDto updateDto = new UpdateLocationDto("Hall Updated", "New Address", 300, "New Desc");

        // Act
        mockMvc.perform(put(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(ADMIN_LOGIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk());

        // Verify
        assertCached(LOCATION_CACHE, other.getId());
    }

    @Test
    void shouldNotEvictCacheWhenLocationNotFoundForUpdateLocation() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);
        performGetLocationById(saved.getId());
        performGetLocations();
        assertCached(LOCATION_CACHE, saved.getId());
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        UpdateLocationDto updateDto = new UpdateLocationDto("Hall Updated", "New Address", 300, "New Desc");

        // Act
        mockMvc.perform(put(BASE_URL + "/999")
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(ADMIN_LOGIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isNotFound());

        // Verify
        assertCached(LOCATION_CACHE, saved.getId());
        assertCached(LOCATIONS_CACHE, ALL_KEY);
    }

    @Test
    void shouldNotEvictCacheWhenNameIsDuplicatedForUpdateLocation() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);
        saveLocation("Hall 2", 200);
        performGetLocationById(saved.getId());
        performGetLocations();
        assertCached(LOCATION_CACHE, saved.getId());
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        UpdateLocationDto updateDto = new UpdateLocationDto("Hall 2", "New Address", 300, "New Desc");

        // Act
        mockMvc.perform(put(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(ADMIN_LOGIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());

        // Verify
        assertCached(LOCATION_CACHE, saved.getId());
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        assertEquals("Hall 1", locationRepository.findById(saved.getId()).orElseThrow().getName());
    }

    @Test
    void shouldNotEvictCacheWhenUserRoleForUpdateLocation() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);
        performGetLocationById(saved.getId());
        assertCached(LOCATION_CACHE, saved.getId());
        UpdateLocationDto updateDto = new UpdateLocationDto("Hall Updated", "New Address", 300, "New Desc");

        // Act
        mockMvc.perform(put(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isForbidden());

        // Verify
        assertCached(LOCATION_CACHE, saved.getId());
        assertEquals("Hall 1", locationRepository.findById(saved.getId()).orElseThrow().getName());
    }

    // deleteLocation()

    @Test
    void shouldEvictLocationAndAllLocationsCacheForDeleteLocation() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);
        performGetLocationById(saved.getId());
        performGetLocations();
        assertCached(LOCATION_CACHE, saved.getId());
        assertCached(LOCATIONS_CACHE, ALL_KEY);

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(ADMIN_LOGIN)))
                .andDo(print())
                // Assert
                .andExpect(status().isNoContent());

        // Verify
        assertEvicted(LOCATION_CACHE, saved.getId());
        assertEvicted(LOCATIONS_CACHE, ALL_KEY);
        assertTrue(locationRepository.findAll().isEmpty());
        mockMvc.perform(get(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotEvictOtherLocationCacheForDeleteLocation() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);
        LocationEntity other = saveLocation("Hall 2", 200);
        performGetLocationById(other.getId());
        assertCached(LOCATION_CACHE, other.getId());

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(ADMIN_LOGIN)))
                .andDo(print())
                // Assert
                .andExpect(status().isNoContent());

        // Verify
        assertCached(LOCATION_CACHE, other.getId());
    }

    @Test
    void shouldNotEvictCacheWhenLocationNotFoundForDeleteLocation() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);
        performGetLocationById(saved.getId());
        performGetLocations();
        assertCached(LOCATION_CACHE, saved.getId());
        assertCached(LOCATIONS_CACHE, ALL_KEY);

        // Act
        mockMvc.perform(delete(BASE_URL + "/999")
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(ADMIN_LOGIN)))
                .andDo(print())
                // Assert
                .andExpect(status().isNotFound());

        // Verify
        assertCached(LOCATION_CACHE, saved.getId());
        assertCached(LOCATIONS_CACHE, ALL_KEY);
    }

    @Test
    void shouldNotEvictCacheWhenUserRoleForDeleteLocation() throws Exception {
        // Arrange
        LocationEntity saved = saveLocation("Hall 1", 100);
        performGetLocationById(saved.getId());
        performGetLocations();
        assertCached(LOCATION_CACHE, saved.getId());
        assertCached(LOCATIONS_CACHE, ALL_KEY);

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + saved.getId())
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andDo(print())
                // Assert
                .andExpect(status().isForbidden());

        // Verify
        assertCached(LOCATION_CACHE, saved.getId());
        assertCached(LOCATIONS_CACHE, ALL_KEY);
        assertEquals(1, locationRepository.findAll().size());
    }

    private void performGetLocationById(Long id) throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + id)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andExpect(status().isOk());
    }

    private void performGetLocations() throws Exception {
        mockMvc.perform(get(BASE_URL)
                        .param(CACHE_MODE_PARAM, SPRING_MODE)
                        .header("Authorization", getAuthHeader(USER_LOGIN)))
                .andExpect(status().isOk());
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

    private void assertCached(String cacheName, Object key) {
        assertNotNull(getCache(cacheName).get(key));
    }

    private void assertEvicted(String cacheName, Object key) {
        assertNull(getCache(cacheName).get(key));
    }

    private Cache getCache(String name) {
        return Objects.requireNonNull(cacheManager.getCache(name));
    }

    private void clearLocationsAndCache() {
        locationRepository.deleteAllInBatch();
        getCache(LOCATION_CACHE).clear();
        getCache(LOCATIONS_CACHE).clear();
    }
}
