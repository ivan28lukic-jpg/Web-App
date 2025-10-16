package group19.WebFinanceApp.controller.dto.response;

import group19.WebFinanceApp.model.CategoryType;

import java.time.Instant;

public class CategoryResponse {
    private Long id;
    private Long ownerId; // null za global
    private String name;
    private CategoryType type;
    private String color;
    private boolean archived;
    private Instant createdAt;

    // NOVO
    private boolean predefined; // globalna/predefinisana
    private boolean mine;       // pripada korisniku (owner != null)

    public CategoryResponse() {}

    public CategoryResponse(Long id, Long ownerId, String name, CategoryType type,
                            String color, boolean archived, Instant createdAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.type = type;
        this.color = color;
        this.archived = archived;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public CategoryType getType() { return type; }
    public void setType(CategoryType type) { this.type = type; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public boolean isPredefined() { return predefined; }
    public void setPredefined(boolean predefined) { this.predefined = predefined; }

    public boolean isMine() { return mine; }
    public void setMine(boolean mine) { this.mine = mine; }
}