package git.xlamid.eventmanagerservice.security.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank(message = "Пароль не может быть пустым")
@Size(min = 8, max = 128, message = "Пароль должен содержать от 8 до 128 символов")
@Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).*$",
        message = """
                Пароль должен содержать минимум одну цифру, одну строчную и одну прописную букву, а также специальный \
                символ\
                """
)
public @interface Password {

    String message() default "Ненадежный пароль";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}