package group19.WebFinanceApp.security;

import io.jsonwebtoken.Claims;
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

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwt;
    private final TokenBlacklist blacklist;

    public JwtAuthFilter(JwtUtil jwt, TokenBlacklist blacklist) {
        this.jwt = jwt;
        this.blacklist = blacklist;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        final String uri = req.getRequestURI();
        final String method = req.getMethod();

        // Pusti login, logout i preflight da prođu bez obavezne autentikacije
        if ("OPTIONS".equalsIgnoreCase(method)
                || "/api/auth/login".equals(uri)
                || "/api/auth/logout".equals(uri)) {
            chain.doFilter(req, res);
            return;
        }

        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(req, res);
            return;
        }

        String token = header.substring(7).trim();

        // Ako je token opozvan – tretiraj kao da ne postoji

        if (!token.isBlank() && blacklist.isRevoked(token)) {
            SecurityContextHolder.clearContext();
            chain.doFilter(req, res);
            return;
        }

        try {
            Claims c = jwt.parse(token);
            String username = c.getSubject();
            String rawRole  = String.valueOf(c.get("role"));           // "USER" ili "ADMIN"
            String authority = rawRole.startsWith("ROLE_") ? rawRole : "ROLE_" + rawRole;

            var auth = new UsernamePasswordAuthenticationToken(
                    username, null, List.of(new SimpleGrantedAuthority(authority))
            );
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(req, res);
    }

}