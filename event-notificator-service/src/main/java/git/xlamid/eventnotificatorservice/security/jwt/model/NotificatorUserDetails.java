package git.xlamid.eventnotificatorservice.security.jwt.model;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class NotificatorUserDetails extends User {

    private final Long id;

    public NotificatorUserDetails(Long id,
                                  String username,
                                  String password,
                                  String userRole) {
        this.id = id;
        Collection<? extends GrantedAuthority> authorities =
                new ArrayList<>(List.of(new SimpleGrantedAuthority(userRole)));
        super(username, password, authorities);
    }
}