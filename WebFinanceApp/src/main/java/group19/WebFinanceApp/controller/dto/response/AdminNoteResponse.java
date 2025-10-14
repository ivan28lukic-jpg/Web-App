package group19.WebFinanceApp.controller.dto.response;

import java.time.Instant;

public class AdminNoteResponse {
    private Long id;
    private Long userId;
    private Long adminId;
    private String adminUsername;  // praktično za UI
    private String note;
    private Instant createdAt;

    public AdminNoteResponse(Long id, Long userId, Long adminId,
                             String adminUsername, String note, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.adminId = adminId;
        this.adminUsername = adminUsername;
        this.note = note;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getAdminId() { return adminId; }
    public String getAdminUsername() { return adminUsername; }
    public String getNote() { return note; }
    public Instant getCreatedAt() { return createdAt; }
}