package git.xlamid.eventmanagerservice.security.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class EventManagerUserDetails extends User {

    private final Long id;
    private final Integer age;

    public EventManagerUserDetails(Long id,
                                   Integer age,
                                   String username,
                                   @NotNull String password,
                                   Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.age = age;
        super(username, password, authorities);
    }
}