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

/**
 * Configuração de segurança base do backend.
 *
 * Esta versão e intencionalmente minima para desbloquear o arranque tecnico.
 * A autenticacao real de dispositivos por API key fica para implementacao manual.
 */
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

    /**
     * Objetivo: definir regras HTTP de seguranca iniciais.
     * Inputs esperados: HttpSecurity providenciado pelo Spring.
     * Output esperado: SecurityFilterChain ativo para toda a aplicacao.
     * Passos logicos a implementar:
     * 1) Introduzir filtro custom de API key para /api/device/**.
     * 2) Definir politicas diferentes para endpoints admin vs device.
     * 3) Ativar tratamento uniforme de erros de autenticacao/autorizacao.
     * Notas: manter Swagger acessivel em ambiente de desenvolvimento.
     */
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
                        .requestMatchers("/api/operators/**").hasAnyRole("OPERATOR", "SUPERVISOR", "ADMIN")
                        .requestMatchers("/api/supervisor/**").hasAnyRole("SUPERVISOR", "ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().denyAll()
                );
        return http.build();
    }
}

