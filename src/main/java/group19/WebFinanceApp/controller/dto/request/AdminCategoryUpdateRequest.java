package group19.WebFinanceApp.controller.dto.request;

import group19.WebFinanceApp.model.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdminCategoryUpdateRequest {

    @NotBlank
    @Size(max = 80)
    private String name;

    // Dozvoli i promenu type (po potrebi); ako ne želiš, ukloni ovo polje
    private CategoryType type; // može biti null -> ne menja se

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be in format #RRGGBB")
    private String color; // može biti null

    // opcioni flag – ako ga ne pošalješ, ne menja se
    private Boolean archived;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Boolean getArchived() { return archived; }
    public void setArchived(Boolean archived) { this.archived = archived; }
}