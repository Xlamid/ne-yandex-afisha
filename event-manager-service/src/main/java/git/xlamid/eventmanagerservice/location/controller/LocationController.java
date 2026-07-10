package git.xlamid.eventmanagerservice.location.controller;

import git.xlamid.eventmanagerservice.location.dto.CreateLocationDto;
import git.xlamid.eventmanagerservice.location.dto.GetLocationDto;
import git.xlamid.eventmanagerservice.location.dto.UpdateLocationDto;
import git.xlamid.eventmanagerservice.location.service.LocationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/locations")
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<GetLocationDto> createLocation(@Valid @RequestBody CreateLocationDto dto) {
        log.info("Creating location: {}", dto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(locationService.createLocation(dto));
    }

    @GetMapping
    public ResponseEntity<List<GetLocationDto>> getLocations() {
        log.info("Getting locations");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(locationService.getLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GetLocationDto> getLocationById(@PathVariable Long id) {
        log.info("Getting location with id: {}", id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(locationService.getLocationById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GetLocationDto> updateLocation(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateLocationDto dto) {
        log.info("Updating location: {} with id {}", dto, id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(locationService.updateLocation(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        log.info("Deleting location with id: {}", id);
        locationService.deleteLocation(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}