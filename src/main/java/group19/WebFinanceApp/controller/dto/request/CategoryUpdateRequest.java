package group19.WebFinanceApp.controller.dto.request;

import group19.WebFinanceApp.model.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;

public class CategoryUpdateRequest {

    @NotBlank
    private String name;

    @NotNull
    private CategoryType type; // dozvoljavamo promenu tipa

    @Pattern(regexp = "^#([A-Fa-f0-9]{6})$", message = "Color must be in format #RRGGBB")
    private String color;

    private boolean archived;

    public CategoryUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }
}