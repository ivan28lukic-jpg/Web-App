package group19.WebFinanceApp.controller.dto.response;

import group19.WebFinanceApp.model.Role;
import java.time.LocalDate;

public class UserResponse {

    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Role role;
    private boolean blocked;
    private LocalDate birthDate;            // može biti null
    private String avatarPath;              // može biti null
    private String preferredCurrencyCode;   // npr. "RSD", "EUR" (može biti null)

    public UserResponse() {}

    public UserResponse(Long id, String firstName, String lastName,
                        String username, String email, Role role, boolean blocked, LocalDate birthDate,
                        String avatarPath, String preferredCurrencyCode) {
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
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public String getPreferredCurrencyCode() { return preferredCurrencyCode; }
    public void setPreferredCurrencyCode(String preferredCurrencyCode) { this.preferredCurrencyCode = preferredCurrencyCode; }
}
