package git.xlamid.eventmanagerservice.location.service;

import git.xlamid.eventmanagerservice.cache.enums.CacheMode;
import git.xlamid.eventmanagerservice.location.dto.CreateLocationDto;
import git.xlamid.eventmanagerservice.location.dto.GetLocationDto;
import git.xlamid.eventmanagerservice.location.dto.UpdateLocationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpringCacheLocationService implements LocationService {

    private final DbLocationService dbLocationService;
    
    @Override
    @CacheEvict(value = "locations", key = "'all'")
    public GetLocationDto createLocation(CreateLocationDto locationDto) {
        return dbLocationService.createLocation(locationDto);
    }

    @Override
    @Cacheable(value = "locations", key = "'all'")
    public List<GetLocationDto> getLocations() {
        return dbLocationService.getLocations();
    }

    @Override
    @Cacheable(value = "location", key = "#id")
    public GetLocationDto getLocationById(Long id) {
        return dbLocationService.getLocationById(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "location", key = "#id"),
            @CacheEvict(value = "locations", key = "'all'")
    })
    public GetLocationDto updateLocation(Long id, UpdateLocationDto locationDto) {
        return dbLocationService.updateLocation(id, locationDto);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "location", key = "#id"),
            @CacheEvict(value = "locations", key = "'all'")
    })
    public void deleteLocation(Long id) {
        dbLocationService.deleteLocation(id);
    }

    @Override
    public CacheMode getCacheMode() {
        return CacheMode.SPRING;
    }
}