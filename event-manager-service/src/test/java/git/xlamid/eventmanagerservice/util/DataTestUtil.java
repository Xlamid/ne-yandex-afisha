package git.xlamid.eventmanagerservice.util;

import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import git.xlamid.eventmanagerservice.location.repository.LocationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataTestUtil {

    private static final String DEFAULT_LOCATION_NAME = "Location";

    @Autowired
    private LocationRepository locationRepository;

    public LocationEntity getTestLocation() {
        return locationRepository.findByName(DEFAULT_LOCATION_NAME)
                .orElse(createLocation(DEFAULT_LOCATION_NAME));
    }

    private LocationEntity createLocation(String name) {
        return locationRepository.save(new LocationEntity(
                null,
                name,
                "123 Event St.",
                1000,
                "Big stage",
                null
        ));
    }
}