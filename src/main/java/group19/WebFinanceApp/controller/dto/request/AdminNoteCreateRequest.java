package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Kreiranje napomene - adminId se NE prosleđuje u requestu,
 * admin se uzima iz SecurityContext-a (Authentication).
 */
public class AdminNoteCreateRequest {

    @NotBlank
    private String note;

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}