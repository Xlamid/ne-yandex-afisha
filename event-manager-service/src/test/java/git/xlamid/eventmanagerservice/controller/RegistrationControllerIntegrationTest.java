package git.xlamid.eventmanagerservice.controller;

import git.xlamid.eventmanagerservice.AbstractWithContainerTest;
import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.event.model.enums.EventStatus;
import git.xlamid.eventmanagerservice.event.repository.EventRepository;
import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import git.xlamid.eventmanagerservice.registration.entity.RegistrationEntity;
import git.xlamid.eventmanagerservice.registration.repository.RegistrationRepository;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import git.xlamid.eventmanagerservice.util.DataTestUtil;
import git.xlamid.eventmanagerservice.util.EventCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class RegistrationControllerIntegrationTest extends AbstractWithContainerTest {

    private static final String BASE_URL = "/events/registrations";

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private DataTestUtil dataTestUtil;
    @Autowired
    private RegistrationRepository registrationRepository;
    @Autowired
    private EventCreator eventCreator;

    private UserEntity testUser;
    private UserEntity testAdmin;
    private LocationEntity testLocation;

    @BeforeEach
    public void setup() {
        testAdmin = userTestUtil.getTestUserByLogin("admin1");
        testUser = userTestUtil.getTestUserByLogin("user1");
        testLocation = dataTestUtil.getTestLocation();
    }

    // registrationUserOnEventByEventId()

    @Test
    void shouldCreateRegistrationForRegistrationUserOnEventByEventId() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "New Event",
                testUser,
                EventStatus.WAIT_START,
                testLocation
        ));

        // Act
        mockMvc.perform(post(BASE_URL + "/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isOk());

        // Verify
        List<RegistrationEntity> registrations = registrationRepository.findAll();
        assertEquals(1, registrations.size());
        assertEquals(savedEvent.getId(), registrations.getFirst().getEvent().getId());
        assertEquals(testUser.getId(), registrations.getFirst().getUser().getId());
    }

    @Test
    void shouldReturnBadRequestWhenEventAlreadyStartedForRegistrationUserOnEventByEventId() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Started Event",
                testUser,
                EventStatus.STARTED,
                testLocation
        ));

        // Act
        mockMvc.perform(post(BASE_URL + "/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());

        // Verify
        assertTrue(registrationRepository.findAll().isEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenRegistrationAlreadyExistsForRegistrationUserOnEventByEventId() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "New Event",
                testUser,
                EventStatus.WAIT_START,
                testLocation
        ));

        // Act
        mockMvc.perform(post(BASE_URL + "/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser.getLogin())))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(post(BASE_URL + "/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Already exists exception"));

        // Verify
        assertEquals(1, registrationRepository.findAll().size());
    }

    @Test
    void shouldReturnForbiddenWhenUserWithRoleAdminForRegistrationUserOnEventByEventId() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "New Event",
                testUser,
                EventStatus.WAIT_START,
                testLocation
        ));

        // Act
        mockMvc.perform(post(BASE_URL + "/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testAdmin.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isForbidden());

        // Verify
        assertTrue(registrationRepository.findAll().isEmpty());
    }

    @Test
    void shouldReturnNotFoundWhenEventDoesNotExistForRegistrationUserOnEventByEventId() throws Exception {
        // Act
        mockMvc.perform(post(BASE_URL + "/99999")
                        .header("Authorization", getAuthHeader(testUser.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isNotFound());
    }

    // getEventsForUser()

    @Test
    void shouldReturnEventsForUserForGetEventsForUser() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "My Registered Event",
                testUser,
                EventStatus.WAIT_START,
                testLocation
        ));
        registrationRepository.save(new RegistrationEntity(
                null,
                OffsetDateTime.now(),
                false,
                testUser,
                savedEvent
        ));

        // Act
        mockMvc.perform(get(BASE_URL + "/my")
                        .header("Authorization", getAuthHeader(testUser.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("My Registered Event"));
    }

    // cancelRegistrationForUserByEventId()

    @Test
    void shouldCancelRegistrationForCancelRegistrationForUserByEventId() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Event To Cancel Reg",
                testUser,
                EventStatus.WAIT_START,
                testLocation
        ));
        RegistrationEntity registration = registrationRepository.save(
                new RegistrationEntity(
                        null,
                        OffsetDateTime.now(),
                        false,
                        testUser,
                        savedEvent
                )
        );

        // Act
        mockMvc.perform(delete(BASE_URL + "/cancel/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isNoContent());

        // Verify
        RegistrationEntity updatedReg = registrationRepository.findById(registration.getId()).orElseThrow();
        assertTrue(updatedReg.isCanceled());
    }

    @Test
    void shouldReturnNotFoundWhenRegistrationDoesNotExistForCancelRegistrationForUserByEventId() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Event",
                testUser,
                EventStatus.WAIT_START,
                testLocation
        ));

        // Act
        mockMvc.perform(delete(BASE_URL + "/cancel/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturnBadRequestWhenEventUnavailableForCancelRegistrationForUserByEventId() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Finished Event",
                testUser,
                EventStatus.FINISHED,
                testLocation
        ));
        registrationRepository.save(new RegistrationEntity(
                null,
                OffsetDateTime.now(),
                false,
                testUser,
                savedEvent
        ));

        // Act
        mockMvc.perform(delete(BASE_URL + "/cancel/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenRegistrationAlreadyCanceledForCancelRegistrationForUserByEventId() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Event To Cancel Reg",
                testUser,
                EventStatus.WAIT_START,
                testLocation
        ));
        registrationRepository.save(
                new RegistrationEntity(
                        null,
                        OffsetDateTime.now(),
                        true,
                        testUser,
                        savedEvent
                )
        );

        // Act
        mockMvc.perform(delete(BASE_URL + "/cancel/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());
    }
}