package git.xlamid.eventmanagerservice.controller;

import git.xlamid.eventcommon.kafka.model.FieldChange;
import git.xlamid.eventcommon.kafka.model.NotificationKafkaEvent;
import git.xlamid.eventcommon.kafka.model.enums.EventType;
import git.xlamid.eventmanagerservice.AbstractWithContainerTest;
import git.xlamid.eventmanagerservice.event.dto.CreateEventDto;
import git.xlamid.eventmanagerservice.event.dto.UpdateEventDto;
import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.event.model.enums.EventStatus;
import git.xlamid.eventmanagerservice.event.repository.EventRepository;
import git.xlamid.eventmanagerservice.event.service.EventService;
import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import git.xlamid.eventmanagerservice.user.entity.UserEntity;
import git.xlamid.eventmanagerservice.util.DataTestUtil;
import git.xlamid.eventmanagerservice.util.EventCreator;
import jakarta.transaction.Transactional;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class EventKafkaIntegrationTest extends AbstractWithContainerTest {

    @Value("${spring.kafka.topic.notification}")
    private String topicName;

    @Autowired
    private DataTestUtil dataTestUtil;
    @Autowired
    private EventCreator eventCreator;
    @Autowired
    private EventService eventService;
    @Autowired
    private EventRepository eventRepository;

    private Consumer<String, String> consumer;
    private UserEntity testUser1;
    private LocationEntity testLocation;

    @BeforeEach
    public void setup() {
        testUser1 = userTestUtil.getTestUserByLogin("user1");
        testLocation = dataTestUtil.getTestLocation();

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                KAFKA_CONTAINER.getBootstrapServers(),
                "event-notification-test",
                true
        );
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        ConsumerFactory<String, String> cf = new DefaultKafkaConsumerFactory<>(
                consumerProps,
                new StringDeserializer(),
                new StringDeserializer()
        );
        consumer = cf.createConsumer();
        consumer.subscribe(List.of(topicName));
        KafkaTestUtils.getRecords(consumer, Duration.ofMillis(100));
    }

    @AfterEach
    public void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    // createEvent()

    @Test
    void shouldSendEventCreatedMessageToKafkaForCreateEvent() throws Exception {
        // Arrange
        CreateEventDto createDto = new CreateEventDto(
                "Kafka Test Event",
                OffsetDateTime.now().plusDays(5),
                500,
                120,
                100,
                testLocation.getId()
        );

        // Act
        mockMvc.perform(post("/events")
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated());

        // Assert
        ConsumerRecord<String, String> record = KafkaTestUtils
                .getSingleRecord(consumer, topicName, Duration.ofSeconds(1));
        assertNotNull(record);

        NotificationKafkaEvent kafkaEvent = objectMapper
                .readValue(record.value(), NotificationKafkaEvent.class);
        assertNotNull(kafkaEvent.getMessageId());
        assertEquals(EventType.EVENT_CREATED, kafkaEvent.getEventType());
        assertEquals(testUser1.getId(), kafkaEvent.getChangedById());

        FieldChange nameChange = getFieldChange(kafkaEvent, "name");
        assertNull(nameChange.getOldValue());
        assertEquals("Kafka Test Event", nameChange.getNewValue());
    }

    // updateEventById()

    @Test
    void shouldSendEventUpdatedMessageToKafkaForUpdateEventById() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Old Name",
                testUser1,
                EventStatus.WAIT_START,
                testLocation
        ));

        UpdateEventDto updateDto = new UpdateEventDto(
                "New Name",
                savedEvent.getDate(),
                300,
                savedEvent.getDuration(),
                savedEvent.getMaxPlaces(),
                testLocation.getId()
        );

        // Act
        mockMvc.perform(put("/events/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk());

        // Assert
        ConsumerRecord<String, String> record = KafkaTestUtils
                .getSingleRecord(consumer, topicName, Duration.ofSeconds(1));
        NotificationKafkaEvent kafkaEvent = objectMapper
                .readValue(record.value(), NotificationKafkaEvent.class);

        assertEquals(EventType.EVENT_UPDATED, kafkaEvent.getEventType());
        assertEquals(testUser1.getId(), kafkaEvent.getChangedById());
        assertEquals(2, kafkaEvent.getChanges().size());

        FieldChange nameChange = getFieldChange(kafkaEvent, "name");
        assertEquals("Old Name", nameChange.getOldValue());
        assertEquals("New Name", nameChange.getNewValue());

        FieldChange costChange = getFieldChange(kafkaEvent, "cost");
        assertEquals(updateDto.getCost(), costChange.getNewValue());
    }

    @Test
    void shouldNotSendMessageToKafkaWhenValidationFailsForUpdateEventById() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Valid Event",
                testUser1,
                EventStatus.WAIT_START,
                testLocation
        ));

        UpdateEventDto invalidDto = new UpdateEventDto(
                "New Name",
                OffsetDateTime.now().minusDays(1),
                300,
                60,
                100,
                testLocation.getId()
        );

        // Act
        mockMvc.perform(put("/events/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser1.getLogin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        // Assert
        ConsumerRecords<String, String> records = KafkaTestUtils
                .getRecords(consumer, Duration.ofSeconds(1));
        assertTrue(records.isEmpty());
    }

    // deleteEventById()

    @Test
    void shouldSendEventCancelledMessageToKafkaForDeleteEventById() throws Exception {
        // Arrange
        EventEntity savedEvent = eventRepository.save(eventCreator.createEventEntity(
                "Event To Cancel",
                testUser1,
                EventStatus.WAIT_START,
                testLocation
        ));

        // Act
        mockMvc.perform(delete("/events/" + savedEvent.getId())
                        .header("Authorization", getAuthHeader(testUser1.getLogin())))
                .andExpect(status().isNoContent());

        // Assert
        ConsumerRecord<String, String> record = KafkaTestUtils
                .getSingleRecord(consumer, topicName, Duration.ofSeconds(1));
        NotificationKafkaEvent kafkaEvent = objectMapper
                .readValue(record.value(), NotificationKafkaEvent.class);

        assertEquals(EventType.EVENT_CANCELLED, kafkaEvent.getEventType());
        assertEquals(testUser1.getId(), kafkaEvent.getChangedById());

        FieldChange statusChange = getFieldChange(kafkaEvent, "status");
        assertEquals(EventStatus.WAIT_START.name(), statusChange.getOldValue());
        assertEquals(EventStatus.CANCELLED.name(), statusChange.getNewValue());
    }

    // updateEvents() - scheduled

    @Test
    void shouldSendEventUpdatedMessageWithNullUserWhenScheduledTaskStartsEventForUpdateEvents() {
        // Arrange
        eventRepository.save(eventCreator.createCustomEventEntity(
                "Ready Event",
                OffsetDateTime.now().minusMinutes(10),
                1000,
                60,
                EventStatus.WAIT_START,
                testUser1,
                testLocation
        ));

        // Act
        eventService.updateEvents();

        // Assert
        ConsumerRecord<String, String> record = KafkaTestUtils
                .getSingleRecord(consumer, topicName, Duration.ofSeconds(1));
        NotificationKafkaEvent kafkaEvent = objectMapper
                .readValue(record.value(), NotificationKafkaEvent.class);

        assertEquals(EventType.EVENT_UPDATED, kafkaEvent.getEventType());
        assertNull(kafkaEvent.getChangedById());

        FieldChange statusChange = getFieldChange(kafkaEvent, "status");
        assertEquals(EventStatus.WAIT_START.name(), statusChange.getOldValue());
        assertEquals(EventStatus.STARTED.name(), statusChange.getNewValue());
    }

    @Test
    void shouldSendEventUpdatedMessageWithNullUserWhenScheduledTaskFinishesEventForUpdateEvents() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();
        eventRepository.save(eventCreator.createCustomEventEntity(
                "Running Event",
                now.minusMinutes(120),
                1000,
                60,
                EventStatus.STARTED,
                testUser1,
                testLocation
        ));

        // Act
        eventService.updateEvents();

        // Assert
        ConsumerRecord<String, String> record = KafkaTestUtils
                .getSingleRecord(consumer, topicName, Duration.ofSeconds(1));
        NotificationKafkaEvent kafkaEvent = objectMapper
                .readValue(record.value(), NotificationKafkaEvent.class);

        assertEquals(EventType.EVENT_UPDATED, kafkaEvent.getEventType());
        assertNull(kafkaEvent.getChangedById());

        FieldChange statusChange = getFieldChange(kafkaEvent, "status");
        assertEquals(EventStatus.STARTED.name(), statusChange.getOldValue());
        assertEquals(EventStatus.FINISHED.name(), statusChange.getNewValue());
    }

    // helpers

    private FieldChange getFieldChange(NotificationKafkaEvent event, String fieldName) {
        return event.getChanges().stream()
                .filter(c -> c.getField().equals(fieldName))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError("Field change for '" + fieldName + "' not found"));
    }
}