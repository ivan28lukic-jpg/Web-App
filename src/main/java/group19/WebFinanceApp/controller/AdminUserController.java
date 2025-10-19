package group19.WebFinanceApp.controller;

import group19.WebFinanceApp.controller.dto.response.AdminUserResponse;
import group19.WebFinanceApp.model.User;
import group19.WebFinanceApp.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserRepository users;

    public AdminUserController(UserRepository users) {
        this.users = users;
    }

    // Lista korisnika sa opcionalnom pretragom
    @GetMapping
    public Page<AdminUserResponse> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String q
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> result;

        if (q == null || q.isBlank()) {
            result = users.findAll(pageable);
        } else {
            result = users.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                    q, q, q, q, pageable
            );
        }

        return result.map(this::toResponse);
    }

    // Blokiranje / odblokiranje korisnika
    @PatchMapping("/{id}/block")
    public ResponseEntity<AdminUserResponse> setBlocked(
            @PathVariable Long id,
            @RequestParam boolean blocked
    ) {
        return users.findById(id)
                .map(u -> {
                    u.setBlocked(blocked);
                    User saved = users.save(u);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private AdminUserResponse toResponse(User u) {
        return new AdminUserResponse(
                u.getId(), u.getFirstName(), u.getLastName(),
                u.getUsername(), u.getEmail(),
                u.getRole(), u.isBlocked(),
                u.getBirthDate(), u.getAvatarPath(),
                u.getPreferredCurrencyCode(), u.getRegisteredAt()
        );
    }
}