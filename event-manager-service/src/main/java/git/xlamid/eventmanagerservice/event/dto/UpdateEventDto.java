package git.xlamid.eventmanagerservice.event.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventDto {

    private String name;

    private OffsetDateTime date;

    @Min(1)
    private Integer cost;

    @Min(30)
    private Integer duration;

    @Min(1)
    private Integer maxPlaces;

    @Min(0)
    private Long locationId;
}