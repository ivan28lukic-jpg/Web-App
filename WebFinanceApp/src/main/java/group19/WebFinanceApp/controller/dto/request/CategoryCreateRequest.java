package group19.WebFinanceApp.controller.dto.request;

import group19.WebFinanceApp.model.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class CategoryCreateRequest {

    // Ako je null -> global category
    private Long ownerId;

    @NotBlank
    private String name;

    @NotNull
    private CategoryType type; // INCOME / EXPENSE

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be in format #RRGGBB")
    private String color;

    public CategoryCreateRequest() {}

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}