package git.xlamid.eventmanagerservice.location.service;

import git.xlamid.eventmanagerservice.cache.enums.CacheMode;
import git.xlamid.eventmanagerservice.location.dto.CreateLocationDto;
import git.xlamid.eventmanagerservice.location.dto.GetLocationDto;
import git.xlamid.eventmanagerservice.location.dto.UpdateLocationDto;

import java.util.List;

public interface LocationService {

    GetLocationDto createLocation(CreateLocationDto locationDto);
    List<GetLocationDto> getLocations();
    GetLocationDto getLocationById(Long id);
    GetLocationDto updateLocation(Long id, UpdateLocationDto locationDto);
    void deleteLocation(Long id);
    CacheMode getCacheMode();
}