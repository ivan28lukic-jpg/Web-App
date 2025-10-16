package group19.WebFinanceApp.controller;

import group19.WebFinanceApp.controller.dto.request.UserCreateRequest;
import group19.WebFinanceApp.controller.dto.request.UserUpdateRequest;
import group19.WebFinanceApp.controller.dto.response.UserResponse;
import group19.WebFinanceApp.model.Role;
import group19.WebFinanceApp.model.User;
import group19.WebFinanceApp.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.security.core.Authentication;
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository users;

    public UserController(UserRepository users) {
        this.users = users;
    }

    @GetMapping("/count")
    public long count() {
        return users.count();
    }

    @GetMapping
    public List<UserResponse> all() {
        return users.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> byId(@PathVariable Long id) {
        return users.findById(id)
                .map(u -> ResponseEntity.ok(toResponse(u)))
                .orElseGet(() -> ResponseEntity.notFound().<UserResponse>build());
    }
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        // Username je subject iz tokena
        return users.findByUsername(authentication.getName())
                .map(u -> ResponseEntity.ok(toResponse(u)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest in) {
        if (users.existsByUsername(in.getUsername()) || users.existsByEmail(in.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).<UserResponse>build();
        }
        User u = new User();
        u.setFirstName(in.getFirstName());
        u.setLastName(in.getLastName());
        u.setUsername(in.getUsername());
        u.setBirthDate(in.getBirthDate());
        u.setEmail(in.getEmail());
        u.setPasswordHash(in.getPassword());  // TODO: hash
        u.setRole(Role.USER);
        u.setBlocked(false);

        u = users.save(u);
        return ResponseEntity.ok(toResponse(u));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UserUpdateRequest in) {
        return users.findById(id).map(u -> {
            if (users.existsByUsernameAndIdNot(in.getUsername(), u.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).<UserResponse>build();
            }
            if (users.existsByEmailAndIdNot(in.getEmail(), u.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).<UserResponse>build();
            }

            u.setFirstName(in.getFirstName());
            u.setLastName(in.getLastName());
            u.setUsername(in.getUsername());
            u.setBirthDate(in.getBirthDate());
            u.setEmail(in.getEmail());
            u.setBirthDate(in.getBirthDate());
            u.setAvatarPath(in.getAvatarPath());
            u.setPreferredCurrencyCode(in.getPreferredCurrencyCode());

            User saved = users.save(u);
            return ResponseEntity.ok(toResponse(saved));
        }).orElseGet(() -> ResponseEntity.notFound().<UserResponse>build());
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(
                u.getId(),
                u.getFirstName(),
                u.getLastName(),
                u.getUsername(),
                u.getEmail(),
                u.getRole(),
                u.isBlocked(),
                u.getBirthDate(),
                u.getAvatarPath(),
                u.getPreferredCurrencyCode()
        );
    }
}