package git.xlamid.eventnotificatorservice.security.config;

import git.xlamid.eventnotificatorservice.exception.handler.EventNotificatorAccessDeniedHandler;
import git.xlamid.eventnotificatorservice.exception.handler.EventNotificatorAuthenticationEntryPoint;
import git.xlamid.eventnotificatorservice.security.jwt.filter.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class EventNotificatiorSecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;
    private final EventNotificatorAuthenticationEntryPoint eventNotificatorAuthenticationEntryPoint;
    private final EventNotificatorAccessDeniedHandler eventNotificatorAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests ->
                        requests
                                // EventNotificationController
                                .requestMatchers("/notifications")
                                    .hasAnyAuthority("ADMIN", "USER")

                                .anyRequest().authenticated())
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(eventNotificatorAuthenticationEntryPoint)
                                .accessDeniedHandler(eventNotificatorAccessDeniedHandler)
                )
                .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}