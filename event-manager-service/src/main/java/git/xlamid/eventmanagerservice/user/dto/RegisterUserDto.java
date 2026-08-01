package git.xlamid.eventmanagerservice.user.dto;

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

    @ToString.Exclude
    private String password;

    @Max(150)
    @NotNull
    private Integer age;
}