package git.xlamid.eventmanagerservice.user.service;

import git.xlamid.eventmanagerservice.security.model.EventManagerUserDetails;
import git.xlamid.eventmanagerservice.user.model.enums.UserModel;
import git.xlamid.eventmanagerservice.user.model.enums.UserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class UserSecurityContextService {

    public Long getUserIdFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        EventManagerUserDetails userDetails =
                (EventManagerUserDetails) Objects.requireNonNull(auth).getPrincipal();
        return Objects.requireNonNull(userDetails).getId();
    }

    public UserModel getUserFromSecurityContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        EventManagerUserDetails userDetails =
                (EventManagerUserDetails) Objects.requireNonNull(auth).getPrincipal();
        String role = String.valueOf(
                Objects.requireNonNull(userDetails).getAuthorities().stream().findFirst()
                        .orElseThrow(() -> new IllegalArgumentException
                                ("Role for user with id: " + userDetails.getId() + "incorrect")));
        return new UserModel(
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getAge(),
                UserRole.valueOf(role)
        );
    }
}