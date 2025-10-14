package group19.WebFinanceApp.controller.admin;

import group19.WebFinanceApp.controller.dto.request.AdminNoteCreateRequest;
import group19.WebFinanceApp.controller.dto.request.AdminNoteUpdateRequest;
import group19.WebFinanceApp.controller.dto.response.AdminNoteResponse;
import group19.WebFinanceApp.model.AdminNote;
import group19.WebFinanceApp.model.User;
import group19.WebFinanceApp.repository.AdminNoteRepository;
import group19.WebFinanceApp.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminUserNotesController {

    private final AdminNoteRepository notes;
    private final UserRepository users;

    public AdminUserNotesController(AdminNoteRepository notes, UserRepository users) {
        this.notes = notes;
        this.users = users;
    }

    @GetMapping("/users/{userId}/notes")
    public Page<AdminNoteResponse> listNotes(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return notes.findByUser_IdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @PostMapping("/users/{userId}/notes")
    public ResponseEntity<AdminNoteResponse> createNote(
            @PathVariable Long userId,
            @Valid @RequestBody AdminNoteCreateRequest req
    ) {
        User user = users.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        User admin = users.findById(req.getAdminId()).orElse(null);
        if (admin == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        AdminNote note = new AdminNote();
        note.setUser(user);
        note.setAdmin(admin);
        note.setNote(req.getNote());

        AdminNote saved = notes.save(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/notes/{noteId}")
    public ResponseEntity<AdminNoteResponse> updateNote(
            @PathVariable Long noteId,
            @Valid @RequestBody AdminNoteUpdateRequest req
    ) {
        return notes.findById(noteId)
                .map(n -> {
                    n.setNote(req.getNote());
                    AdminNote saved = notes.save(n);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/notes/{noteId}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long noteId) {
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