package git.xlamid.eventmanagerservice.location.controller;

import git.xlamid.eventmanagerservice.cache.enums.CacheMode;
import git.xlamid.eventmanagerservice.location.dto.CreateLocationDto;
import git.xlamid.eventmanagerservice.location.dto.GetLocationDto;
import git.xlamid.eventmanagerservice.location.dto.UpdateLocationDto;
import git.xlamid.eventmanagerservice.location.service.LocationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/locations")
public class LocationController {

    private final Map<CacheMode, LocationService> locationServices;

    public LocationController(List<LocationService> locationServices) {
        this.locationServices = locationServices.stream()
                .collect(Collectors.toMap(LocationService::getCacheMode, Function.identity()));
    }

    @PostMapping
    public ResponseEntity<GetLocationDto> createLocation(@Valid @RequestBody CreateLocationDto dto,
                                                         @RequestParam(defaultValue = "DB") CacheMode cacheMode) {
        log.info("Creating location: {}", dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(locationServices.get(cacheMode).createLocation(dto));
    }

    @GetMapping
    public ResponseEntity<List<GetLocationDto>> getLocations(@RequestParam(defaultValue = "DB") CacheMode cacheMode) {
        log.info("Getting locations");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(locationServices.get(cacheMode).getLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetLocationDto> getLocationById(@PathVariable Long id,
                                                          @RequestParam(defaultValue = "DB") CacheMode cacheMode) {
        log.info("Getting location with id: {}", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(locationServices.get(cacheMode).getLocationById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GetLocationDto> updateLocation(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateLocationDto dto,
                                                         @RequestParam(defaultValue = "DB") CacheMode cacheMode) {
        log.info("Updating location: {} with id {}", dto, id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(locationServices.get(cacheMode).updateLocation(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id,
                                               @RequestParam(defaultValue = "DB") CacheMode cacheMode) {
        log.info("Deleting location with id: {}", id);
        locationServices.get(cacheMode).deleteLocation(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}