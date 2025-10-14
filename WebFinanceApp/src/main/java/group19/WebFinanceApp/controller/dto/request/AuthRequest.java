package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AuthRequest {
    @NotBlank private String username;
    @NotBlank private String password;

    public String getUsername() { return username; }
    public String getPassword() { return password; }
}