package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class UserUpdateRequest {

    @NotBlank
    @Size(max = 80)
    private String firstName;

    @NotBlank
    @Size(max = 80)
    private String lastName;

    @NotBlank
    @Size(max = 80)
    private String username;

    @Email
    @NotBlank
    @Size(max = 160)
    private String email;

    @NotNull
    private LocalDate birthDate;               // opcionalno
    @Size(max = 255)
    private String avatarPath;                 // opcionalno

    @Size(max = 3)
    private String preferredCurrencyCode;      // npr. RSD/EUR, opcionalno

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getBirthDate() { return birthDate; }
    public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }

    public String getAvatarPath() { return avatarPath; }
    public void setAvatarPath(String avatarPath) { this.avatarPath = avatarPath; }

    public String getPreferredCurrencyCode() { return preferredCurrencyCode; }
    public void setPreferredCurrencyCode(String preferredCurrencyCode) { this.preferredCurrencyCode = preferredCurrencyCode; }
}