package group19.WebFinanceApp.controller;

import group19.WebFinanceApp.controller.dto.RegisterRequest;
import group19.WebFinanceApp.controller.dto.request.AuthRequest;
import group19.WebFinanceApp.controller.dto.response.AuthResponse;
import group19.WebFinanceApp.model.Role;
import group19.WebFinanceApp.model.User;
import group19.WebFinanceApp.repository.UserRepository;
import group19.WebFinanceApp.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import group19.WebFinanceApp.security.TokenBlacklist;

import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;
    private final long expiresSeconds;
    private final TokenBlacklist tokenBlacklist;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtUtil jwt,
                          @org.springframework.beans.factory.annotation.Value("${app.jwt.expires}") long expiresSeconds,
                          TokenBlacklist tokenBlacklist) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
        this.expiresSeconds = expiresSeconds;
        this.tokenBlacklist = tokenBlacklist;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest req) {
        User u = users.findByUsername(req.getUsername())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (u.isBlocked())
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "User is blocked");

        // Password check (bcrypt)
        if (!encoder.matches(req.getPassword(), u.getPasswordHash()))
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid credentials");

        String token = jwt.generateToken(u.getUsername(), u.getId(), u.getRole().name());
        String exp = Instant.now().plusSeconds(expiresSeconds).toString();

        return ResponseEntity.ok(new AuthResponse(token, u.getId(), u.getRole().name(), exp));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }
        String token = authHeader.substring(7).trim();
        var claims = jwt.parse(token); // dobijamo exp iz tokena
        java.time.Instant exp = claims.getExpiration().toInstant();
        // ubaci token u crnu listu do isteka
        tokenBlacklist.revoke(token, exp);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        // Provera da li već postoji korisnik sa tim username-om
        if (users.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.status(409).body("Username already exists");
        }
        // Provera da li email već postoji
        if (users.existsByEmail(req.getEmail())) {
            return ResponseEntity.status(409).body("Email already exists");
        }

        // Napravi novog korisnika
        User u = new User();
        u.setUsername(req.getUsername());
        u.setPasswordHash(encoder.encode(req.getPassword()));
        u.setEmail(req.getEmail());
        u.setFirstName(req.getFirstName());
        u.setLastName(req.getLastName());
        u.setBirthDate(req.getBirthDate());
        u.setRole(Role.USER);
        u.setBlocked(false);

        users.save(u);

        // Vrati neki odgovor
        return ResponseEntity.ok("User registered");
    }
}