package group19.WebFinanceApp.controller.dto.response;

import group19.WebFinanceApp.model.CategoryType;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionResponse {

    private Long id;
    private Long walletId;

    private Long categoryId;
    private String categoryName;
    private CategoryType categoryType;

    private BigDecimal amount;
    private String description;
    private Instant occurredAt;

    private String transferId;

    private Long recurringTemplateId;
    private Long recurringInstanceId;

    public TransactionResponse() {}

    // stari konstruktor (ostavljen da postojeći kod i dalje radi)
    public TransactionResponse(Long id,
                               Long walletId,
                               Long categoryId,
                               String categoryName,
                               CategoryType categoryType,
                               BigDecimal amount,
                               String description,
                               Instant occurredAt,
                               String transferId) {
        this.id = id;
        this.walletId = walletId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.categoryType = categoryType;
        this.amount = amount;
        this.description = description;
        this.occurredAt = occurredAt;
        this.transferId = transferId;
    }

    // novi konstruktor sa recurring poljima
    public TransactionResponse(Long id,
                               Long walletId,
                               Long categoryId,
                               String categoryName,
                               CategoryType categoryType,
                               BigDecimal amount,
                               String description,
                               Instant occurredAt,
                               String transferId,
                               Long recurringTemplateId,
                               Long recurringInstanceId) {
        this(id, walletId, categoryId, categoryName, categoryType, amount, description, occurredAt, transferId);
        this.recurringTemplateId = recurringTemplateId;
        this.recurringInstanceId = recurringInstanceId;
    }

    // --- getters & setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public CategoryType getCategoryType() { return categoryType; }
    public void setCategoryType(CategoryType categoryType) { this.categoryType = categoryType; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public String getTransferId() { return transferId; }
    public void setTransferId(String transferId) { this.transferId = transferId; }

    public Long getRecurringTemplateId() { return recurringTemplateId; }
    public void setRecurringTemplateId(Long recurringTemplateId) { this.recurringTemplateId = recurringTemplateId; }

    public Long getRecurringInstanceId() { return recurringInstanceId; }
    public void setRecurringInstanceId(Long recurringInstanceId) { this.recurringInstanceId = recurringInstanceId; }
}