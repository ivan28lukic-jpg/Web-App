package group19.WebFinanceApp.controller.dto.response;

public class AuthResponse {
    private String token;
    private Long userId;
    private String role;
    private String expiresAt;

    public AuthResponse(String token, Long userId, String role, String expiresAt) {
        this.token = token; this.userId = userId; this.role = role; this.expiresAt = expiresAt;
    }
    public String getToken() { return token; }
    public Long getUserId() { return userId; }
    public String getRole() { return role; }
    public String getExpiresAt() { return expiresAt; }
}