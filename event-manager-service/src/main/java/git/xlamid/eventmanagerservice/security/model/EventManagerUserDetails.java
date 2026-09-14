package git.xlamid.eventmanagerservice.security.model;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class EventManagerUserDetails extends User {

    private final Long id;
    private final Integer age;

    public EventManagerUserDetails(Long id,
                                   Integer age,
                                   String username,
                                   @NotNull String password,
                                   String userRole) {
        this.id = id;
        this.age = age;
        Collection<? extends GrantedAuthority> authorities =
                new ArrayList<>(List.of(new SimpleGrantedAuthority(userRole)));
        super(username, password, authorities);
    }
}