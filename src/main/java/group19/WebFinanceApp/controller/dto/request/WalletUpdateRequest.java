package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public class WalletUpdateRequest {

    @NotBlank
    private String name;

    private boolean archived;

    public WalletUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}