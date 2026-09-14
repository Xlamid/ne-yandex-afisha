package git.xlamid.eventmanagerservice.event.util;

import git.xlamid.eventmanagerservice.event.entity.EventEntity;
import git.xlamid.eventmanagerservice.event.repository.EventRepository;
import git.xlamid.eventmanagerservice.exception.model.notfound.EventNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventFinder {

    private final EventRepository eventRepository;

    public EventEntity findEventById(Long id) {
        return eventRepository.findByIdWithLocationIdAndUserIdAndRegistrationId(id)
                .orElseThrow(() -> new EventNotFoundException("Event with id: " + id + " not found"));
    }
}