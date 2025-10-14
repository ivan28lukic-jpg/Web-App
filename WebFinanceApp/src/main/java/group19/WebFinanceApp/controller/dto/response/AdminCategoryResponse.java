package group19.WebFinanceApp.controller.dto.response;

import group19.WebFinanceApp.model.CategoryType;

import java.time.Instant;

public class AdminCategoryResponse {
    private Long id;
    private String name;
    private CategoryType type;
    private String color;
    private boolean archived;
    private Instant createdAt;

    public AdminCategoryResponse(Long id, String name, CategoryType type,
                                 String color, boolean archived, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.color = color;
        this.archived = archived;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public CategoryType getType() { return type; }
    public String getColor() { return color; }
    public boolean isArchived() { return archived; }
    public Instant getCreatedAt() { return createdAt; }
}