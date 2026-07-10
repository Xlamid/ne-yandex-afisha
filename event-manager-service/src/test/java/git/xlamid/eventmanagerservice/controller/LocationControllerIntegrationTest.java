package git.xlamid.eventmanagerservice.controller;

import git.xlamid.eventmanagerservice.AbstractWithContainerTest;
import git.xlamid.eventmanagerservice.location.dto.CreateLocationDto;
import git.xlamid.eventmanagerservice.location.dto.UpdateLocationDto;
import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import git.xlamid.eventmanagerservice.location.repository.LocationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
public class LocationControllerIntegrationTest extends AbstractWithContainerTest {

    private static final String BASE_URL = "/locations";

    @Autowired
    private LocationRepository locationRepository;

    @Test
    void shouldCreateLocationAndReturnCreatedAndSavedDto() throws Exception {
        // Arrange
        CreateLocationDto createDto = new CreateLocationDto(
                "Conference Hall A",
                "123 Main St, Springfield",
                500,
                "A large hall for conferences"
        );

        // Act
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(createDto.getName()))
                .andExpect(jsonPath("$.address").value(createDto.getAddress()))
                .andExpect(jsonPath("$.capacity").value(createDto.getCapacity()))
                .andExpect(jsonPath("$.description").value(createDto.getDescription()));

        // Verify
        List<LocationEntity> locations = locationRepository.findAll();
        assertEquals(1, locations.size());
        assertEquals(createDto.getName(), locations.getFirst().getName());
    }

    @Test
    void shouldReturnBadRequestWhenInvalidCapacity() throws Exception {
        // Arrange
        CreateLocationDto createDto = new CreateLocationDto(
                "Conference Hall A",
                "123 Main St, Springfield",
                0,
                "A large hall for conferences"
        );

        // Act
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                // Assert
                .andExpect(status().isBadRequest());

        // Verify
        assertTrue(locationRepository.findAll().isEmpty());
    }

    @Test
    void shouldReturnListOfLocations() throws Exception {
        // Arrange
        LocationEntity loc1 = new LocationEntity(null, "Hall 1", "Address 1", 100, "Desc 1");
        LocationEntity loc2 = new LocationEntity(null, "Hall 2", "Address 2", 200, "Desc 2");
        locationRepository.saveAll(List.of(loc1, loc2));

        // Act
        mockMvc.perform(get(BASE_URL))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value(loc1.getName()))
                .andExpect(jsonPath("$[1].name").value(loc2.getName()));
    }

    @Test
    void shouldReturnLocationByIdWhenExists() throws Exception {
        // Arrange
        LocationEntity savedLocation = locationRepository.save(
                new LocationEntity(null, "Hall 1", "Address 1", 100, "Desc 1")
        );

        // Act
        mockMvc.perform(get(BASE_URL + "/" + savedLocation.getId()))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedLocation.getId()))
                .andExpect(jsonPath("$.name").value(savedLocation.getName()));
    }

    @Test
    void shouldReturnNotFoundByIdWhenNotExists() throws Exception {
        // Act
        mockMvc.perform(get(BASE_URL + "/" + "999"))
                .andDo(print())
                // Assert
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldUpdateAndReturnLocation() throws Exception {
        // Arrange
        LocationEntity savedLocation = locationRepository.save(
                new LocationEntity(null, "Old Hall", "Old Address", 100, "Old Desc")
        );

        UpdateLocationDto updateDto = new UpdateLocationDto(
                "New Hall",
                "New Address",
                300,
                "New Desc"
        );

        // Act
        mockMvc.perform(put(BASE_URL + "/" + savedLocation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(updateDto.getName()))
                .andExpect(jsonPath("$.capacity").value(updateDto.getCapacity()));

        // Verify
        LocationEntity updatedEntity = locationRepository
                .findById(savedLocation.getId()).orElseThrow();
        assertEquals(updateDto.getName(), updatedEntity.getName());
        assertEquals(updateDto.getCapacity(), updatedEntity.getCapacity());
    }

    @Test
    void shouldRemoveFromDatabase() throws Exception {
        // Arrange
        LocationEntity savedLocation = locationRepository.save(
                new LocationEntity(
                        null,
                        "Hall to delete",
                        "Delete address",
                        50,
                        "Desc"
                )
        );

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + savedLocation.getId()))
                .andDo(print())
                // Assert
                .andExpect(status().isNoContent());

        // Verify
        assertTrue(locationRepository.findById(savedLocation.getId()).isEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenNameAlreadyExistsOnCreate() throws Exception {
        // Arrange
        LocationEntity existingLocation = new LocationEntity(null, "Duplicate Name", "Address 1", 100, "Desc 1");
        locationRepository.save(existingLocation);

        CreateLocationDto createDto = new CreateLocationDto(
                "Duplicate Name",
                "123 Main St, Springfield",
                500,
                "A large hall for conferences"
        );

        // Act
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Repeatable validation exception"))
                .andExpect(jsonPath("$.detailedMessage").value("Location with name '" + createDto.getName() + "' already exists"))
                .andExpect(jsonPath("$.dateTime").exists());
    }

    @Test
    void shouldReturnBadRequestWhenNameAlreadyExistsOnUpdate() throws Exception {
        // Arrange
        LocationEntity loc1 = locationRepository.save(new LocationEntity(null, "Existing Name", "Address 1", 100, "Desc 1"));
        LocationEntity loc2 = locationRepository.save(new LocationEntity(null, "Other Name", "Address 2", 200, "Desc 2"));

        UpdateLocationDto updateDto = new UpdateLocationDto(
                "Existing Name",
                "New Address",
                300,
                "New Desc"
        );

        // Act
        mockMvc.perform(put(BASE_URL + "/" + loc2.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Repeatable validation exception"));
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentLocation() throws Exception {
        // Arrange
        UpdateLocationDto updateDto = new UpdateLocationDto("Name", "Address", 100, "Desc");

        // Act
        mockMvc.perform(put(BASE_URL + "/" + "999999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not found exception"))
                .andExpect(jsonPath("$.detailedMessage").value("Location with id 999999 not found"));
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistentLocation() throws Exception {
        // Act
        mockMvc.perform(delete(BASE_URL + "/" + "999999"))
                .andDo(print())
                // Assert
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Not found exception"));
    }

    @Test
    void shouldReturnBadRequestWithMultipleValidationErrors() throws Exception {
        // Arrange
        CreateLocationDto createDto = new CreateLocationDto(
                "",
                "A",
                null,
                "A large hall for conferences"
        );

        // Act
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation exception"))
                .andExpect(jsonPath("$.detailedMessage").exists());
    }
}