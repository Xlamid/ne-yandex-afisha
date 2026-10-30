package git.xlamid.eventmanagerservice.event.service;

import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.event.repository.EventRepository;
import git.xlamid.eventmanagerservice.event.util.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Stream;

import static git.xlamid.eventcommon.kafka.model.enums.EventType.EVENT_FINISHED;
import static git.xlamid.eventcommon.kafka.model.enums.EventType.EVENT_STARTED;
import static git.xlamid.eventmanagerservice.event.model.enums.EventStatus.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventScheduler {

    private final EventRepository eventRepository;
    private final EventActionNotificator eventActionNotificator;
    private final CacheManager cacheManager;

    @Scheduled(fixedRateString = "${scheduled.fixed-rate.event-status}")
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void updateEvents() {
        List<Long> updatedIds = Stream.concat(
                updateStartedEvents().stream(), 
                updateFinishedEvents().stream()
        ).toList();
        if (!updatedIds.isEmpty()) {
            Cache eventCache = cacheManager.getCache("events");
            if (eventCache != null) {
                updatedIds.forEach(eventCache::evict);
                log.info("evict cache for {} events to started", updatedIds.size());
            }
        }
    }

    private List<Long> updateStartedEvents() {
        List<EventEntity> startedEvents = eventRepository
                .findAllForStartedEventsWithStatus(WAIT_START.name(), OffsetDateTime.now());
        if (!startedEvents.isEmpty()) {
            log.info("update {} events to started", startedEvents.size());
            startedEvents.forEach(eventEntity ->
                    eventActionNotificator.applyActionAndNotify(eventEntity, EVENT_STARTED, false,
                            eEntity -> eEntity.setStatus(STARTED.name())));
        }
        return startedEvents.stream()
                .map(EventEntity::getId).toList();
    }

    private List<Long> updateFinishedEvents() {
        List<EventEntity> finishedEvents = eventRepository
                .findAllForFinishedEventsWithStatus(STARTED.name(), OffsetDateTime.now());
        if (!finishedEvents.isEmpty()) {
            log.info("update {} events to finished", finishedEvents.size());
            finishedEvents.forEach(eventEntity ->
                    eventActionNotificator.applyActionAndNotify(eventEntity, EVENT_FINISHED, false,
                            eEntity -> eEntity.setStatus(FINISHED.name())));
        }
        return finishedEvents.stream()
                .map(EventEntity::getId).toList();
    }
}