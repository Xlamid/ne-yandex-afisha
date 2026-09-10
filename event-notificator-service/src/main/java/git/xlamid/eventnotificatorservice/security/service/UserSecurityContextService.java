package git.xlamid.eventnotificatorservice.security.service;

import git.xlamid.eventnotificatorservice.security.jwt.model.NotificatorUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserSecurityContextService {

    public Long getUserIdFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        NotificatorUserDetails userDetails =
                (NotificatorUserDetails) Objects.requireNonNull(auth).getPrincipal();
        return Objects.requireNonNull(userDetails).getId();
    }
}