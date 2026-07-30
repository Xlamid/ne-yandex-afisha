package git.xlamid.eventmanagerservice.user.dto;

import git.xlamid.eventmanagerservice.user.model.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetUserDto {

    private Long id;
    private String login;
    private Integer age;
    private UserRole role;
}