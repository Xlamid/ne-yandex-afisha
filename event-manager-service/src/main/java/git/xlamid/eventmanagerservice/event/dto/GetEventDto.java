package git.xlamid.eventmanagerservice.event.dto;

import git.xlamid.eventmanagerservice.event.model.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class GetEventDto {

    private Long id;
    private String name;
    private OffsetDateTime date;
    private Integer cost;
    private Integer duration;
    private Integer maxPlaces;
    private Integer occupiedPlaces;
    private EventStatus status;
    private Long locationId;
    private Long ownerId;
}