package git.xlamid.eventmanagerservice.user.model.enums;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {

    private Long id;
    private String login;
    private Integer age;
    private UserRole role;
}