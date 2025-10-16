// src/main/java/group19/WebFinanceApp/security/JwtUtil.java
package group19.WebFinanceApp.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey key;
    private final long expiresSeconds;

    // Injekcija vrednosti iz application.properties / application.yml
    public JwtUtil(
            @Value("${jwt.secret:change-me-32-bytes-minimum-change-me-32-b}") String secret,
            @Value("${jwt.expiresSeconds:3600}") long expiresSeconds
    ) {
        // HS256: ključ min ~32 bajta
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiresSeconds = expiresSeconds;
    }

    /** Generiše token; subject = username, dodatni claim-ovi: uid, role */
    public String generateToken(String username, Long userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expiresSeconds)))
                .claims(Map.of(
                        "uid", userId,
                        "role", role
                ))
                .signWith(key)
                .compact();
    }

    /** Parsira potpisan JWT i vraća payload (Claims). Baca izuzetak ako nije validan. */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Brza validacija bez bacanja izuzetka. */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** Getteri za tipične informacije iz tokena. */
    public String getUsername(String token) { return parse(token).getSubject(); }

    public Long getUserId(String token) {
        Object v = parse(token).get("uid");
        return v == null ? null : (v instanceof Number ? ((Number) v).longValue() : Long.valueOf(v.toString()));
        // napomena: ako je uid serijalizovan kao Integer/Long/String, ovo ga pouzdano konvertuje u Long
    }

    public String getRole(String token) {
        Object v = parse(token).get("role");
        return v == null ? null : v.toString();
    }

    public long getExpiresSeconds() { return expiresSeconds; }
}