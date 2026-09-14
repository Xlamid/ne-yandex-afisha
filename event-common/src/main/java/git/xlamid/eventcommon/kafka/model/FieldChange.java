package git.xlamid.eventcommon.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FieldChange {

    private String field;
    private Object oldValue;
    private Object newValue;
}