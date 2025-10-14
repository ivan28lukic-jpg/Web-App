package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AdminNoteUpdateRequest {

    @NotBlank
    private String note;

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}