package git.xlamid.eventmanagerservice.location.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLocationDto {

    @NotBlank
    @Size(min = 3, max = 300)
    private String name;

    @NotBlank
    @Size(min = 3, max = 1000)
    private String address;

    @Min(1)
    @Max(1000000)
    @NotNull
    private Integer capacity;

    private String description;
}