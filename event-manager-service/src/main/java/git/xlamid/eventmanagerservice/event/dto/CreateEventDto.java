package git.xlamid.eventmanagerservice.event.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.OffsetDateTime;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventDto {

    @NotBlank
    private String name;

    @NotNull
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