package git.xlamid.eventmanagerservice.user.dto;

import git.xlamid.eventmanagerservice.security.validation.Password;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserDto {

    @NotBlank
    @Size(min = 3, max = 300)
    private String login;

    @Password
    @ToString.Exclude
    private String password;

    @Min(18)
    @Max(150)
    @NotNull
    private Integer age;
}