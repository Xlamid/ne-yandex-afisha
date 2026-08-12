package git.xlamid.eventmanagerservice.event.dto;

import git.xlamid.eventmanagerservice.event.model.enums.EventStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventSearchRequestDto {

    private String name;
    private Integer placesMin;
    private Integer placesMax;
    private OffsetDateTime dateStartAfter;
    private OffsetDateTime dateStartBefore;
    private Integer costMin;
    private Integer costMax;
    private Integer durationMin;
    private Integer durationMax;
    private Long locationId;
    private EventStatus eventStatus;
}