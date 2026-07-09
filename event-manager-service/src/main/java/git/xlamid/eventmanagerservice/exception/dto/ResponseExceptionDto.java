package git.xlamid.eventmanagerservice.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseExceptionDto {

    private String message;
    private String detailedMessage;
    private OffsetDateTime dateTime;
}