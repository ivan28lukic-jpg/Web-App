package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AdminNoteCreateRequest {

    // Pošto (za sada) nemamo security kontekst, adminId dolazi iz request-a
    @NotNull
    private Long adminId;

    @NotBlank
    private String note;

    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}