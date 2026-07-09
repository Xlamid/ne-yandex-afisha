package git.xlamid.eventmanagerservice.location.service;

import git.xlamid.eventmanagerservice.exception.model.notfound.LocationNotFoundException;
import git.xlamid.eventmanagerservice.location.dto.CreateLocationDto;
import git.xlamid.eventmanagerservice.location.dto.GetLocationDto;
import git.xlamid.eventmanagerservice.location.dto.UpdateLocationDto;
import git.xlamid.eventmanagerservice.location.entity.LocationEntity;
import git.xlamid.eventmanagerservice.location.mapper.LocationMapper;
import git.xlamid.eventmanagerservice.location.repository.LocationRepository;
import git.xlamid.eventmanagerservice.location.util.LocationCheckerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationService {

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;
    private final LocationCheckerUtil locationCheckerUtil;

    @Transactional
    public GetLocationDto createLocation(CreateLocationDto locationDto) {
        locationCheckerUtil.checkRepeatableName(locationDto.getName());
        LocationEntity locationEntity = locationMapper.dtoToEntity(locationDto);
        return locationMapper.entityToGetDto(
                locationRepository.save(locationEntity)
        );
    }

    public List<GetLocationDto> getLocations() {
        return locationRepository.findAll()
                .stream()
                .map(locationMapper::entityToGetDto)
                .toList();
    }

    public GetLocationDto getLocationById(Long id) {
        return locationMapper.entityToGetDto(
                findLocationById(id)
        );
    }

    @Transactional
    public GetLocationDto updateLocation(Long id, UpdateLocationDto locationDto) {
        LocationEntity locationEntity = findLocationById(id);
        locationCheckerUtil.checkRepeatableName(id, locationDto.getName());
        locationMapper.updateEntityByDto(locationEntity, locationDto);
        return locationMapper.entityToGetDto(
                locationRepository.save(locationEntity)
        );
    }

    public void deleteLocation(Long id) {
        locationRepository.delete(
                findLocationById(id)
        );
    }

    private LocationEntity findLocationById(Long id) {
        return locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException("Location with id " + id + " not found"));
    }
}