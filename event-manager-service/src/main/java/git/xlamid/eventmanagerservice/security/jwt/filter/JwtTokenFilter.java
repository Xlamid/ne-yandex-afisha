package git.xlamid.eventmanagerservice.security.jwt.filter;

import git.xlamid.eventmanagerservice.exception.model.notfound.NotFoundException;
import git.xlamid.eventmanagerservice.security.jwt.manager.JwtTokenManager;
import git.xlamid.eventmanagerservice.security.service.EventManagerUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenManager jwtTokenManager;
    private final EventManagerUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (token == null || !token.startsWith("Bearer ")) {
            log.error("Incorrect JWT Token: {}", token);
            filterChain.doFilter(request, response);
            return;
        }
        token = token.substring("Bearer ".length());

        String login;
        UserDetails userDetails;
        try {
            login = jwtTokenManager.getLoginFromToken(token);
            userDetails = userDetailsService.loadUserByUsername(login);
        } catch (NotFoundException e) {
            log.error("User with JWT not found", e);
            filterChain.doFilter(request, response);
            return;
        } catch (Exception e) {
            log.error("Invalid JWT Token: {}", token, e);
            filterChain.doFilter(request, response);
            return;
        }

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authToken);
        filterChain.doFilter(request, response);
    }
}