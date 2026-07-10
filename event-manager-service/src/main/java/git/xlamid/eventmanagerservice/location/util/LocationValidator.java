package git.xlamid.eventmanagerservice.location.util;

import git.xlamid.eventmanagerservice.exception.model.validation.RepeatableValidationException;
import git.xlamid.eventmanagerservice.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LocationValidator {

    private final LocationRepository locationRepository;

    public void validateRepeatableName(String name) {
        if (locationRepository.existsByName(name)) {
            throwDuplicateNameException(name);
        }
    }

    public void validateRepeatableName(Long id, String name) {
        if (locationRepository.existsByNameAndIdNot(name, id)) {
            throwDuplicateNameException(name);
        }
    }

    private void throwDuplicateNameException(String name) {
        throw new RepeatableValidationException("Location with name '" + name + "' already exists");
    }
}