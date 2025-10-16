package group19.WebFinanceApp.controller.dto.response;

import group19.WebFinanceApp.model.CategoryType;

import java.math.BigDecimal;
import java.time.Instant;

public class TopTransactionItem {
    private Long id;
    private Long ownerId;
    private Long walletId;

    private Long categoryId;
    private String categoryName;
    private CategoryType categoryType;

    private BigDecimal amount;
    private String description;
    private Instant occurredAt;

    public TopTransactionItem(Long id, Long ownerId, Long walletId,
                              Long categoryId, String categoryName, CategoryType categoryType,
                              BigDecimal amount, String description, Instant occurredAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.walletId = walletId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryType = categoryType;
        this.amount = amount;
        this.description = description;
        this.occurredAt = occurredAt;
    }

    public Long getId() { return id; }
    public Long getOwnerId() { return ownerId; }
    public Long getWalletId() { return walletId; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public CategoryType getCategoryType() { return categoryType; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public Instant getOccurredAt() { return occurredAt; }
}