package git.xlamid.eventmanagerservice.location.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetLocationDto {

    private Long id;
    private String name;
    private String address;
    private Integer capacity;
    private String description;
}