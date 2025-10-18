package group19.WebFinanceApp.controller;

import group19.WebFinanceApp.controller.dto.response.AdminNoteResponse;
import group19.WebFinanceApp.model.User;
import group19.WebFinanceApp.repository.AdminNoteRepository;
import group19.WebFinanceApp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint za korisnika da vidi sopstvene admin napomene.
 * GET /api/users/me/notes
 */
@RestController
@RequestMapping("/api/users")
public class UserNotesController {

    private static final Logger log = LoggerFactory.getLogger(UserNotesController.class);

    private final AdminNoteRepository notes;
    private final UserRepository users;

    public UserNotesController(AdminNoteRepository notes, UserRepository users) {
        this.notes = notes;
        this.users = users;
    }

    @GetMapping("/me/notes")
    public ResponseEntity<Page<AdminNoteResponse>> myNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        String principal = authentication != null ? authentication.getName() : null;
        log.info("myNotes called by '{}', page={}, size={}", principal, page, size);

        if (authentication == null || principal == null) {
            log.warn("myNotes: unauthenticated request");
            return ResponseEntity.status(401).build();
        }

        User user = users.findByUsername(principal).orElse(null);
        if (user == null) {
            log.warn("myNotes: user not found in DB: {}", principal);
            return ResponseEntity.notFound().build();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<AdminNoteResponse> res = notes.findByUser_IdOrderByCreatedAtDesc(user.getId(), pageable)
                .map(n -> {
                    Long adminId = (n.getAdmin() != null) ? n.getAdmin().getId() : null;
                    String adminUsername = (n.getAdmin() != null) ? n.getAdmin().getUsername() : null;
                    return new AdminNoteResponse(
                            n.getId(),
                            n.getUser().getId(),
                            adminId,
                            adminUsername,
                            n.getNote(),
                            n.getCreatedAt()
                    );
                });
        log.info("myNotes: returning {} notes for userId={}", res.getNumberOfElements(), user.getId());
        return ResponseEntity.ok(res);
    }
}