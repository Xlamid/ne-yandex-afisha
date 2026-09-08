package git.xlamid.eventmanagerservice.controller;

import git.xlamid.eventmanagerservice.AbstractWithContainerTest;
import git.xlamid.eventmanagerservice.event.dto.CreateEventDto;
import git.xlamid.eventmanagerservice.event.dto.EventSearchRequestDto;
import git.xlamid.eventmanagerservice.event.dto.UpdateEventDto;
import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.event.repository.EventRepository;
import git.xlamid.eventmanagerservice.event.service.EventService;
import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import git.xlamid.eventmanagerservice.util.DataTestUtil;
import git.xlamid.eventmanagerservice.util.EventCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

import static git.xlamid.eventmanagerservice.event.model.enums.EventStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class EventControllerIntegrationTest extends AbstractWithContainerTest {

    private static final String BASE_URL = "/events";

    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventService eventService;
    @Autowired
    private DataTestUtil dataTestUtil;
    @Autowired
    private EventCreator eventCreator;

    private UserEntity testUser1;
    private UserEntity testUser2;
    private UserEntity testAdmin;
    private LocationEntity testLocation;

    @BeforeEach
    public void setup() {
        testAdmin = userTestUtil.getTestUserByLogin("admin1");
        testUser1 = userTestUtil.getTestUserByLogin("user1");
        testUser2 = userTestUtil.getTestUserByLogin("user2");
        testLocation = dataTestUtil.getTestLocation();
    }

    // createEvent()

    @Test
    void shouldCreateEventAndReturnGetEventDtoForCreateEvent() throws Exception {
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
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(createDto.getName()))
                .andExpect(jsonPath("$.cost").value(createDto.getCost()))
                .andExpect(jsonPath("$.maxPlaces").value(createDto.getMaxPlaces()))
                .andExpect(jsonPath("$.status").value(WAIT_START.name()));

        // Verify
        List<EventEntity> events = eventRepository.findAll();
        assertEquals(1, events.size());
        assertEquals("Summer Festival", events.getFirst().getName());
    }

    @Test
    void shouldReturnBadRequestWhenDateIsTooEarlyForCreateEvent() throws Exception {
        // Arrange
        CreateEventDto createDto = new CreateEventDto(
                "Too Early Event",
                OffsetDateTime.now().plusHours(12),
                500,
                120,
                100,
                testLocation.getId()
        );

        // Act
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());

        // Verify
        assertTrue(eventRepository.findAll().isEmpty());
    }

    @Test
    void shouldReturnBadRequestWhenMaxPlacesExceedsLocationCapacityForCreateEvent() throws Exception {
        // Arrange
        CreateEventDto createDto = new CreateEventDto(
                "Overcrowded Event",
                OffsetDateTime.now().plusDays(5),
                500,
                120,
                1500,
                testLocation.getId()
        );

        // Act
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenCostIsInvalidForCreateEvent() throws Exception {
        // Arrange
        CreateEventDto createDto = new CreateEventDto(
                "Free Event",
                OffsetDateTime.now().plusDays(5),
                0,
                120,
                100,
                testLocation.getId()
        );

        // Act
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Not valid exception"));
    }

    @Test
    void shouldReturnForbiddenWhenUserWithRoleAdminForCreateEvent() throws Exception {
        // Arrange
        CreateEventDto createDto = new CreateEventDto(
                "Free Event",
                OffsetDateTime.now().plusDays(5),
                100,
                120,
                100,
                testLocation.getId()
        );

        // Act
        mockMvc.perform(post(BASE_URL)
                        .header("Authorization", getAuthHeader(testAdmin.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Forbidden"));
    }

    // searchEventsByFilters()

    @Test
    void shouldReturnEventsForSearchEventsByFilters() throws Exception {
        // Arrange
        eventRepository.save(eventCreator.createEventEntity(
                "Java Meetup",
                testAdmin,
                WAIT_START,
                testLocation
        ));
        eventRepository.save(eventCreator.createEventEntity(
                "Python Meetup",
                testAdmin,
                WAIT_START,
                testLocation
        ));

        EventSearchRequestDto searchDto = new EventSearchRequestDto();
        searchDto.setName("Java Meetup");

        // Act
        mockMvc.perform(post(BASE_URL + "/search")
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Java Meetup"));
    }

    @Test
    void shouldReturnEventsWhenUserWithRoleAdminForSearchEventsByFilters() throws Exception {
        // Arrange
        eventRepository.save(eventCreator.createEventEntity(
                "Java Meetup",
                testAdmin,
                WAIT_START,
                testLocation
        ));
        eventRepository.save(eventCreator.createEventEntity(
                "Python Meetup",
                testAdmin,
                WAIT_START,
                testLocation
        ));

        EventSearchRequestDto searchDto = new EventSearchRequestDto();
        searchDto.setName("Java Meetup");

        // Act
        mockMvc.perform(post(BASE_URL + "/search")
                        .header("Authorization", getAuthHeader(testAdmin.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Java Meetup"));
    }

    @Test
    void shouldReturnEventsFilteredByCostRangeForSearchEventsByFilters() throws Exception {
        // Arrange
        eventRepository.save(eventCreator.createCustomEventEntity(
                "Cheap Event",
                OffsetDateTime.now().plusDays(5),
                50,
                60,
                WAIT_START,
                testAdmin,
                testLocation
        ));
        eventRepository.save(eventCreator.createCustomEventEntity(
                "Target Event",
                OffsetDateTime.now().plusDays(5),
                300,
                60,
                WAIT_START,
                testAdmin,
                testLocation
        ));
        eventRepository.save(eventCreator.createCustomEventEntity(
                "Expensive Event",
                OffsetDateTime.now().plusDays(5),
                1000,
                60,
                WAIT_START,
                testAdmin,
                testLocation
        ));

        EventSearchRequestDto searchDto = new EventSearchRequestDto();
        searchDto.setCostMin(100);
        searchDto.setCostMax(500);

        // Act
        mockMvc.perform(post(BASE_URL + "/search")
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Target Event"));
    }

    @Test
    void shouldReturnEventsFilteredByStatusAndDatesForSearchEventsByFilters() throws Exception {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();
        eventRepository.save(eventCreator.createCustomEventEntity(
                "Past Event",
                now.minusDays(2),
                100,
                60,
                FINISHED,
                testAdmin,
                testLocation
        ));
        eventRepository.save(eventCreator.createCustomEventEntity(
                "Future Wait Event",
                now.plusDays(10),
                100,
                60,
                WAIT_START,
                testAdmin,
                testLocation
        ));
        eventRepository.save(eventCreator.createCustomEventEntity(
                "Future Started Event",
                now.plusDays(5),
                100,
                60,
                STARTED,
                testAdmin,
                testLocation
        ));

        EventSearchRequestDto searchDto = new EventSearchRequestDto();
        searchDto.setEventStatus(WAIT_START);
        searchDto.setDateStartAfter(now);

        // Act
        mockMvc.perform(post(BASE_URL + "/search")
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Future Wait Event"));
    }

    @Test
    void shouldReturnEventsSortedByDateDescAndNameAscForSearchEventsByFilters() throws Exception {
        // Arrange
        OffsetDateTime date1 = OffsetDateTime.now().plusDays(5);
        OffsetDateTime date2 = OffsetDateTime.now().plusDays(10);
        eventRepository.save(eventCreator.createCustomEventEntity(
                "A Event",
                date1,
                100,
                60,
                WAIT_START,
                testAdmin,
                testLocation
        ));
        eventRepository.save(eventCreator.createCustomEventEntity(
                "Z Event",
                date2,
                100,
                60,
                WAIT_START,
                testAdmin,
                testLocation
        ));
        eventRepository.save(eventCreator.createCustomEventEntity(
                "B Event",
                date1,
                100,
                60,
                WAIT_START,
                testAdmin,
                testLocation
        ));

        EventSearchRequestDto searchAllDto = new EventSearchRequestDto();

        // Act
        mockMvc.perform(post(BASE_URL + "/search")
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchAllDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].name").value("Z Event"))
                .andExpect(jsonPath("$[1].name").value("A Event"))
                .andExpect(jsonPath("$[2].name").value("B Event"));
    }

    @Test
    void shouldReturnEmptyListWhenNoEventsMatchFiltersForSearchEventsByFilters() throws Exception {
        // Arrange
        eventRepository.save(eventCreator.createEventEntity(
                "Java Meetup",
                testUser1,
                WAIT_START,
                testLocation
        ));

        EventSearchRequestDto searchDto = new EventSearchRequestDto();
        searchDto.setCostMax(10);

        // Act
        mockMvc.perform(post(BASE_URL + "/search")
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // getEventsForUser()

    @Test
    void shouldReturnOnlyEventsOwnedByCurrentUserForGetEventsForUser() throws Exception {
        // Arrange
        eventRepository.save(eventCreator.createEventEntity(
                "User Event 1",
                testUser1,
                WAIT_START,
                testLocation
        ));
        eventRepository.save(eventCreator.createEventEntity(
                "User Event 2",
                testUser1,
                WAIT_START,
                testLocation
        ));
        eventRepository.save(eventCreator.createEventEntity(
                "Other Event",
                testAdmin,
                WAIT_START,
                testLocation
        ));

        // Act
        mockMvc.perform(get(BASE_URL + "/my")
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("User Event 1"))
                .andExpect(jsonPath("$[1].name").value("User Event 2"));
    }

    @Test
    void shouldReturnEmptyListWhenUserHasNoEventsForGetEventsForUser() throws Exception {
        // Arrange
        eventRepository.save(eventCreator.createEventEntity(
                "Other Event",
                testAdmin,
                WAIT_START,
                testLocation
        ));

        // Act
        mockMvc.perform(get(BASE_URL + "/my")
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnForbiddenWhenUserWithRoleAdminForGetEventsForUser() throws Exception {
        // Arrange
        eventRepository.save(eventCreator.createEventEntity(
                "Admin Event",
                testAdmin,
                WAIT_START,
                testLocation
        ));

        // Act
        mockMvc.perform(get(BASE_URL + "/my")
                        .header("Authorization", getAuthHeader(testAdmin.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isForbidden());
    }

    // getEventById()

    @Test
    void shouldReturnEventByIdWhenExistsForGetEventById() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Public Event",
                testUser1,
                WAIT_START,
                testLocation
        ));

        // Act
        mockMvc.perform(get(BASE_URL + "/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedEvent.getId()))
                .andExpect(jsonPath("$.name").value("Public Event"));
    }

    @Test
    void shouldReturnEventByIdWhenUserWithRoleAdminForGetEventById() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Public Event",
                testUser1,
                WAIT_START,
                testLocation
        ));

        // Act
        mockMvc.perform(get(BASE_URL + "/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testAdmin.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedEvent.getId()))
                .andExpect(jsonPath("$.name").value("Public Event"));
    }

    @Test
    void shouldReturnNotFoundWhenNotExistsForGetEventById() throws Exception {
        // Act
        mockMvc.perform(get(BASE_URL + "/99999")
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isNotFound());
    }

    // updateEventById()

    @Test
    void shouldUpdateAndReturnEventForUpdateEventById() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Old Event",
                testUser1,
                WAIT_START,
                testLocation
        ));
        UpdateEventDto updateDto = new UpdateEventDto(
                "Updated Event",
                OffsetDateTime.now().plusDays(3),
                200,
                90,
                80,
                testLocation.getId()
        );

        // Act
        mockMvc.perform(put(BASE_URL + "/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Event"))
                .andExpect(jsonPath("$.cost").value(200));

        // Verify
        EventEntity updatedInDb = eventRepository.findById(savedEvent.getId()).orElseThrow();
        assertEquals("Updated Event", updatedInDb.getName());
        assertEquals(200, updatedInDb.getCost());
    }

    @Test
    void shouldReturnForbiddenWhenNotOwnerTriesToUpdateForUpdateEventById() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Owner Event",
                testUser1,
                WAIT_START,
                testLocation
        ));
        UpdateEventDto updateDto = new UpdateEventDto(
                "Hacked Event",
                OffsetDateTime.now().plusDays(3),
                200,
                90,
                80,
                testLocation.getId()
        );

        // Act
        mockMvc.perform(put(BASE_URL + "/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser2.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnBadRequestWhenMaxPlacesLessThanOccupiedForUpdateEventById() throws Exception {
        // Arrange
        EventEntity event = eventCreator.createEventEntity(
                "Popular Event",
                testUser1,
                WAIT_START,
                testLocation
        );
        event.setOccupiedPlaces(50);
        EventEntity savedEvent = eventRepository.save(event);

        UpdateEventDto updateDto = new UpdateEventDto(
                "Updated Event",
                OffsetDateTime.now().plusDays(3),
                200,
                90,
                40,
                testLocation.getId()
        );

        // Act
        mockMvc.perform(put(BASE_URL + "/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistentEventForUpdateEventById() throws Exception {
        // Arrange
        UpdateEventDto updateDto = new UpdateEventDto(
                "Ghost Event",
                OffsetDateTime.now().plusDays(3),
                200,
                90,
                80,
                testLocation.getId()
        );

        // Act
        mockMvc.perform(put(BASE_URL + "/99999")
                        .header("Authorization", getAuthHeader(testAdmin.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andDo(print())
                // Assert
                .andExpect(status().isNotFound());
    }

    // deleteEventById()

    @Test
    void shouldCancelEventForDeleteEventById() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Event To Cancel",
                testUser1,
                WAIT_START,
                testLocation
        ));

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isNoContent());

        // Verify
        EventEntity cancelledEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        assertEquals(CANCELLED.name(), cancelledEvent.getStatus());
    }

   @Test
    void shouldCancelEventForDeleteWithAdminEventById() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Event To Cancel",
                testUser1,
                WAIT_START,
                testLocation
        ));

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testAdmin.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isNoContent());

        // Verify
        EventEntity cancelledEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        assertEquals(CANCELLED.name(), cancelledEvent.getStatus());
    }

    @Test
    void shouldReturnForbiddenWhenNotOwnerTriesToDeleteForDeleteEventById() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Admin Event",
                testUser1,
                WAIT_START,
                testLocation
        ));

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser2.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnBadRequestWhenEventAlreadyStartedForDeleteEventById() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Started Event",
                testAdmin,
                STARTED,
                testLocation
        ));

        // Act
        mockMvc.perform(delete(BASE_URL + "/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testAdmin.getLogin())))
                .andDo(print())
                // Assert
                .andExpect(status().isBadRequest());
    }

    // updateEvents()

    @Test
    void shouldUpdateStatusToStartedWhenTimeHasComeForUpdateEvents() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();
        EventEntity futureEvent = eventCreator.createCustomEventEntity(
                "Future Event",
                now.plusHours(1),
                1000,
                60,
                WAIT_START,
                testUser1,
                testLocation
        );
        EventEntity readyToStartEvent = eventCreator.createCustomEventEntity(
                "Ready Event",
                now.minusMinutes(10),
                1000,
                60,
                WAIT_START,
                testUser1,
                testLocation
        );
        eventRepository.save(futureEvent);
        eventRepository.save(readyToStartEvent);

        // Act
        eventService.updateEvents();

        // Assert
        EventEntity updatedFutureEvent = eventRepository.findById(futureEvent.getId()).orElseThrow();
        EventEntity updatedReadyEvent = eventRepository.findById(readyToStartEvent.getId()).orElseThrow();

        assertEquals(WAIT_START.name(), updatedFutureEvent.getStatus());
        assertEquals(STARTED.name(), updatedReadyEvent.getStatus());
    }

    @Test
    void shouldUpdateStatusToFinishedWhenTimeHasPassedForUpdateEvents() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();
        EventEntity runningEvent = eventCreator.createCustomEventEntity(
                "Running Event",
                now.minusMinutes(30),
                1000,
                60,
                STARTED,
                testUser1,
                testLocation
        );
        EventEntity finishedEvent = eventCreator.createCustomEventEntity(
                "Finished Event",
                now.minusMinutes(120),
                1000,
                60,
                STARTED,
                testUser1,
                testLocation
        );
        eventRepository.save(runningEvent);
        eventRepository.save(finishedEvent);

        // Act
        eventService.updateEvents();

        // Assert
        EventEntity updatedRunningEvent = eventRepository.findById(runningEvent.getId()).orElseThrow();
        EventEntity updatedFinishedEvent = eventRepository.findById(finishedEvent.getId()).orElseThrow();

        assertEquals(STARTED.name(), updatedRunningEvent.getStatus());
        assertEquals(FINISHED.name(), updatedFinishedEvent.getStatus());
    }

    @Test
    void shouldProcessFullLifecycleCorrectlyForUpdateEvents() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();
        EventEntity future = eventCreator.createCustomEventEntity(
                "Future",
                now.plusHours(2),
                1000,
                60,
                WAIT_START,
                testUser1,
                testLocation
        );
        EventEntity justStarted = eventCreator.createCustomEventEntity(
                "Started",
                now.minusMinutes(5),
                61000,
                60,
                WAIT_START,
                testUser1,
                testLocation
        );
        EventEntity justFinished = eventCreator.createCustomEventEntity(
                "Finished",
                now.minusMinutes(90),
                1000,
                60,
                STARTED,
                testUser1,
                testLocation
        );
        eventRepository.save(future);
        eventRepository.save(justStarted);
        eventRepository.save(justFinished);

        // Act
        eventService.updateEvents();

        // Assert
        assertEquals(WAIT_START.name(), eventRepository.findById(future.getId()).orElseThrow().getStatus());
        assertEquals(STARTED.name(), eventRepository.findById(justStarted.getId()).orElseThrow().getStatus());
        assertEquals(FINISHED.name(), eventRepository.findById(justFinished.getId()).orElseThrow().getStatus());
    }

    @Test
    void shouldNotChangeStatusWhenNoEventsMeetCriteriaForUpdateEvents() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();
        EventEntity farFutureEvent = eventCreator.createCustomEventEntity(
                "Far Future",
                now.plusDays(1),
                1000,
                60,
                WAIT_START,
                testUser1,
                testLocation
        );
        EventEntity ongoingEvent = eventCreator.createCustomEventEntity(
                "Ongoing",
                now.minusMinutes(10),
                1000,
                60,
                STARTED,
                testUser1,
                testLocation
        );
        EventEntity cancelledEvent = eventCreator.createCustomEventEntity(
                "Cancelled",
                now.minusMinutes(100),
                1000,
                60,
                CANCELLED,
                testUser1,
                testLocation
        );
        EventEntity alreadyFinishedEvent = eventCreator.createCustomEventEntity(
                "Already Finished",
                now.minusMinutes(200),
                1000,
                60,
                FINISHED,
                testUser1,
                testLocation
        );
        eventRepository.save(farFutureEvent);
        eventRepository.save(ongoingEvent);
        eventRepository.save(cancelledEvent);
        eventRepository.save(alreadyFinishedEvent);

        // Act
        eventService.updateEvents();

        // Assert
        assertEquals(WAIT_START.name(), eventRepository.findById(farFutureEvent.getId()).orElseThrow().getStatus());
        assertEquals(STARTED.name(), eventRepository.findById(ongoingEvent.getId()).orElseThrow().getStatus());
        assertEquals(CANCELLED.name(), eventRepository.findById(cancelledEvent.getId()).orElseThrow().getStatus());
        assertEquals(FINISHED.name(), eventRepository.findById(alreadyFinishedEvent.getId()).orElseThrow().getStatus());
    }

    @Test
    void shouldExecuteWithoutErrorsWhenDatabaseIsEmptyForUpdateEvents() {
        // Arrange
        long initialCount = eventRepository.count();
        assertEquals(0, initialCount);

        // Act & Assert
        assertDoesNotThrow(() -> eventService.updateEvents());
        assertEquals(0, eventRepository.count());
    }
}