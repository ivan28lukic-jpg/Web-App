package group19.WebFinanceApp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table (name = "users")
public class User{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String firstName;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String lastName;

    @NotBlank
    @Column(nullable = false, length = 80)
    private String username;

    @Email @NotBlank
    @Column(nullable = false, length = 160)
    private String email;

    @NotBlank
    @Column(nullable = false, length = 255)
    private String passwordHash;

    @NotNull
    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Role role = Role.USER;

    @Column(length = 255)
    private String avatarPath;

    @Column(length = 3)
    private String preferredCurrencyCode;

    @Column(nullable = false, updatable = false)
    private Instant registeredAt;

    @Column(nullable = false)
    private boolean blocked = false;

    // Admin beleške (1:N)
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdminNote> adminNotes = new ArrayList<>();

    public User() {}

    @PrePersist
    protected void onRegister() {
        if (this.registeredAt == null) this.registeredAt = Instant.now();
    }

    // --- getters/setters ---
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

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public String getPreferredCurrencyCode() { return preferredCurrencyCode; }
    public void setPreferredCurrencyCode(String preferredCurrencyCode) { this.preferredCurrencyCode = preferredCurrencyCode; }

    public Instant getRegisteredAt() { return registeredAt; }
    public void setRegisteredAt(Instant registeredAt) { this.registeredAt = registeredAt; }

    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }

    public List<AdminNote> getAdminNotes() { return adminNotes; }
    public void setAdminNotes(List<AdminNote> adminNotes) { this.adminNotes = adminNotes; }

    public void addAdminNote(AdminNote note) {
        note.setUser(this);
        this.adminNotes.add(note);
    }
}
