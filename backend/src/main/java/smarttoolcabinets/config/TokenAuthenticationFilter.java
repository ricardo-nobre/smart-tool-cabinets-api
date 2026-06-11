package smarttoolcabinets.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro Bearer minimo para ambiente de desenvolvimento.
 *
 * Mapeamento de tokens:
 * - DEV-TOKEN-* -> ROLE_DEVICE
 * - OPERATOR-TOKEN-* -> ROLE_OPERATOR
 * - SUPERVISOR-TOKEN-* -> ROLE_SUPERVISOR
 * - ADMIN-TOKEN-* -> ROLE_ADMIN
 */
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            UsernamePasswordAuthenticationToken authentication = buildAuthentication(token);
            if (authentication != null) {
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private UsernamePasswordAuthenticationToken buildAuthentication(String token) {
        if (token.startsWith("DEV-TOKEN-")) {
            return createAuthentication(token, "ROLE_DEVICE");
        }
        if (token.startsWith("OPERATOR-TOKEN-")) {
            return createAuthentication(token, "ROLE_OPERATOR");
        }
        if (token.startsWith("SUPERVISOR-TOKEN-")) {
            return createAuthentication(token, "ROLE_SUPERVISOR");
        }
        if (token.startsWith("ADMIN-TOKEN-")) {
            return createAuthentication(token, "ROLE_ADMIN");
        }
        return null;
    }

    private UsernamePasswordAuthenticationToken createAuthentication(String principal, String role) {
        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority(role))
        );
    }
}
