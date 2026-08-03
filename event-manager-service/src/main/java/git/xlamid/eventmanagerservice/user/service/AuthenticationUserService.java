package git.xlamid.eventmanagerservice.user.service;

import git.xlamid.eventmanagerservice.security.jwt.manager.JwtTokenManager;
import git.xlamid.eventmanagerservice.user.dto.AuthUserDto;
import git.xlamid.eventmanagerservice.user.dto.GetUserJwtDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationUserService {

    private final AuthenticationManager authManager;
    private final JwtTokenManager jwtTokenManager;

    public GetUserJwtDto authenticateUser(AuthUserDto dto) {
        authManager.authenticate(new UsernamePasswordAuthenticationToken(
                dto.getLogin(),
                dto.getPassword()
        ));
        return new GetUserJwtDto(
                jwtTokenManager.generateToken(dto.getLogin())
        );
    }
}