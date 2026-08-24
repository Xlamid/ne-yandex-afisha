package git.xlamid.eventmanagerservice.user.service;

import git.xlamid.eventmanagerservice.security.jwt.manager.EventManagerJwtTokenManager;
import git.xlamid.eventmanagerservice.security.model.EventManagerUserDetails;
import git.xlamid.eventmanagerservice.user.dto.AuthUserDto;
import git.xlamid.eventmanagerservice.user.dto.GetUserJwtDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthenticationUserService {

    private final AuthenticationManager authManager;
    private final EventManagerJwtTokenManager jwtTokenManager;

    public GetUserJwtDto authenticateUser(AuthUserDto dto) {
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(
                dto.getLogin(),
                dto.getPassword()
        ));
        EventManagerUserDetails userDetails = (EventManagerUserDetails) auth.getPrincipal();
        return new GetUserJwtDto(jwtTokenManager.generateToken(
                Objects.requireNonNull(userDetails).getUsername(),
                userDetails.getId(),
                getRole(userDetails)
        ));
    }

    private String getRole(EventManagerUserDetails userDetails) {
        return String.valueOf(
                Objects.requireNonNull(userDetails).getAuthorities().stream().findFirst()
                        .orElseThrow(() -> new IllegalArgumentException
                                ("Role for user with id: " + userDetails.getId() + "incorrect")));
    }
}