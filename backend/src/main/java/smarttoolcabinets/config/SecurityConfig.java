package smarttoolcabinets.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import smarttoolcabinets.user.domain.UserRole;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final Environment environment;
    private final TokenAuthenticationFilter tokenAuthenticationFilter;

    public SecurityConfig(
            Environment environment,
            TokenAuthenticationFilter tokenAuthenticationFilter
    ) {
        this.environment = environment;
        this.tokenAuthenticationFilter = tokenAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean isDevProfile = environment.acceptsProfiles(Profiles.of("dev"));

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .anonymous(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> response.sendError(HttpStatus.UNAUTHORIZED.value()))
                        .accessDeniedHandler((request, response, accessDeniedException) -> response.sendError(HttpStatus.FORBIDDEN.value()))
                )
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/device/auth").permitAll()
                        .requestMatchers(isDevProfile ? "/v3/api-docs/**" : "/__swagger-disabled__").permitAll()
                        .requestMatchers(isDevProfile ? "/swagger-ui/**" : "/__swagger-disabled__").permitAll()
                        .requestMatchers(isDevProfile ? "/swagger-ui.html" : "/__swagger-disabled__").permitAll()
                        .requestMatchers("/api/device/**").hasRole("DEVICE")
                        .requestMatchers("/api/operators/**").hasAnyRole(UserRole.OPERATOR, UserRole.SUPERVISOR, UserRole.ADMIN)
                        .requestMatchers("/api/supervisor/**").hasAnyRole(UserRole.SUPERVISOR, UserRole.ADMIN)
                        .requestMatchers("/api/admin/**").hasRole(UserRole.ADMIN)
                        .anyRequest().denyAll()
                );
        return http.build();
    }
}

