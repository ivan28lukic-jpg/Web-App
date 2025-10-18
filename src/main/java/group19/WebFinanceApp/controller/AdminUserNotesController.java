package group19.WebFinanceApp.controller.admin;

import group19.WebFinanceApp.controller.dto.request.AdminNoteCreateRequest;
import group19.WebFinanceApp.controller.dto.request.AdminNoteUpdateRequest;
import group19.WebFinanceApp.controller.dto.response.AdminNoteResponse;
import group19.WebFinanceApp.model.AdminNote;
import group19.WebFinanceApp.model.User;
import group19.WebFinanceApp.repository.AdminNoteRepository;
import group19.WebFinanceApp.repository.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Admin controller za upravljanje napomenama (samo za ROLE_ADMIN).
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserNotesController {

    private static final Logger log = LoggerFactory.getLogger(AdminUserNotesController.class);

    private final AdminNoteRepository notes;
    private final UserRepository users;

    public AdminUserNotesController(AdminNoteRepository notes, UserRepository users) {
        this.notes = notes;
        this.users = users;
    }

    @GetMapping("/users/{userId}/notes")
    public ResponseEntity<Page<AdminNoteResponse>> listNotes(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication
    ) {
        log.info("Admin listNotes called by '{}' for userId={}, page={}, size={}",
                authentication != null ? authentication.getName() : "anonymous",
                userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<AdminNoteResponse> result = notes.findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/users/{userId}/notes")
    public ResponseEntity<AdminNoteResponse> createNote(
            @PathVariable Long userId,
            @Valid @RequestBody AdminNoteCreateRequest req,
            Authentication authentication
    ) {
        log.info("Admin createNote called by '{}' for userId={}, note-len={}",
                authentication != null ? authentication.getName() : "anonymous",
                userId, req.getNote() != null ? req.getNote().length() : 0);

        User user = users.findById(userId).orElse(null);
        if (user == null) {
            log.warn("createNote: target user not found: {}", userId);
            return ResponseEntity.notFound().build();
        }

        if (authentication == null || authentication.getName() == null) {
            log.warn("createNote: no authentication present");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String adminUsername = authentication.getName();
        User admin = users.findByUsername(adminUsername).orElse(null);
        if (admin == null) {
            log.warn("createNote: admin user not found in DB: {}", adminUsername);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        // NEW: prevent admins from creating notes for themselves
        if (admin.getId() != null && admin.getId().equals(user.getId())) {
            log.warn("createNote: admin attempted to create a note for themselves: {}", adminUsername);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        AdminNote note = new AdminNote();
        note.setUser(user);
        note.setAdmin(admin);
        note.setNote(req.getNote());

        AdminNote saved = notes.save(note);
        log.info("createNote: saved note id={} for userId={} by admin={}", saved.getId(), userId, adminUsername);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/notes/{noteId}")
    public ResponseEntity<AdminNoteResponse> updateNote(
            @PathVariable Long noteId,
            @Valid @RequestBody AdminNoteUpdateRequest req,
            Authentication authentication
    ) {
        log.info("Admin updateNote called by '{}', noteId={}", authentication != null ? authentication.getName() : "anonymous", noteId);
        return notes.findById(noteId)
                .map(n -> {
                    n.setNote(req.getNote());
                    AdminNote saved = notes.save(n);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId, Authentication authentication) {
        log.info("Admin deleteNote called by '{}', noteId={}", authentication != null ? authentication.getName() : "anonymous", noteId);
        if (!notes.existsById(noteId)) return ResponseEntity.notFound().build();
        notes.deleteById(noteId);
        return ResponseEntity.noContent().build();
    }

    private AdminNoteResponse toResponse(AdminNote n) {
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
    }
}