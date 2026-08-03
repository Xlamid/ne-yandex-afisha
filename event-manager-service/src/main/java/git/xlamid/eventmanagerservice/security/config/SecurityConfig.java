package git.xlamid.eventmanagerservice.security.config;

import git.xlamid.eventmanagerservice.exception.handler.GlobalAccessDeniedHandler;
import git.xlamid.eventmanagerservice.exception.handler.GlobalAuthenticationEntryPoint;
import git.xlamid.eventmanagerservice.security.jwt.filter.JwtTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;
    private final GlobalAuthenticationEntryPoint globalAuthenticationEntryPoint;
    private final GlobalAccessDeniedHandler globalAccessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        return http
                .formLogin(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(requests ->
                        requests
                                // UserController
                                .requestMatchers(HttpMethod.POST, "/users", "/users/auth")
                                    .permitAll()
                                .requestMatchers(HttpMethod.GET, "/users/{userId}")
                                    .hasAnyAuthority("ADMIN")

                                // LocationController
                                .requestMatchers(HttpMethod.GET, "/locations", "/locations/**")
                                    .hasAnyAuthority("USER", "ADMIN")
                                .requestMatchers("/locations/**")
                                    .hasAnyAuthority("ADMIN")

                                .anyRequest().authenticated())
                .exceptionHandling(exception ->
                        exception
                                .authenticationEntryPoint(globalAuthenticationEntryPoint)
                                .accessDeniedHandler(globalAccessDeniedHandler)
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