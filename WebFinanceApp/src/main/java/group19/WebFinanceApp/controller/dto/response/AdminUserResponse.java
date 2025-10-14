package group19.WebFinanceApp.controller.dto.response;

import group19.WebFinanceApp.model.Role;

import java.time.Instant;
import java.time.LocalDate;

public class AdminUserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Role role;
    private boolean blocked;
    private LocalDate birthDate;
    private String avatarPath;
    private String preferredCurrencyCode;
    private Instant registeredAt;

    public AdminUserResponse(Long id, String firstName, String lastName,
                             String username, String email, Role role, boolean blocked,
                             LocalDate birthDate, String avatarPath,
                             String preferredCurrencyCode, Instant registeredAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.role = role;
        this.blocked = blocked;
        this.birthDate = birthDate;
        this.avatarPath = avatarPath;
        this.preferredCurrencyCode = preferredCurrencyCode;
        this.registeredAt = registeredAt;
    }

    // --- getters ---
    public Long getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public boolean isBlocked() { return blocked; }
    public LocalDate getBirthDate() { return birthDate; }
    public String getAvatarPath() { return avatarPath; }
    public String getPreferredCurrencyCode() { return preferredCurrencyCode; }
    public Instant getRegisteredAt() { return registeredAt; }
}