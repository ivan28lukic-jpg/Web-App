package group19.WebFinanceApp.controller.dto.response;

import group19.WebFinanceApp.model.CategoryType;

import java.math.BigDecimal;
import java.time.Instant;

public class TopTransactionItem {
    private Long id;
    private String ownerUsername;
    private String walletName;

    private Long categoryId;
    private String categoryName;
    private CategoryType categoryType;

    private BigDecimal amount;
    private String description;
    private Instant occurredAt;

    public TopTransactionItem(Long id, String ownerUsername, String walletName,
                              Long categoryId, String categoryName, CategoryType categoryType,
                              BigDecimal amount, String description, Instant occurredAt) {
        this.id = id;
        this.ownerUsername = ownerUsername;
        this.walletName = walletName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryType = categoryType;
        this.amount = amount;
        this.description = description;
        this.occurredAt = occurredAt;
    }

    public Long getId() { return id; }
    public String getOwnerUsername() { return ownerUsername; }
    public String getWalletName() { return walletName; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public CategoryType getCategoryType() { return categoryType; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public Instant getOccurredAt() { return occurredAt; }
}