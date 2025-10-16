package group19.WebFinanceApp.controller.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class SavingGoalResponse {
    private Long id;
    private Long ownerId;
    private Long walletId;
    private String walletCurrencyCode;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate dueDate;
    private boolean archived;
    private Instant createdAt;
    private Instant updatedAt;

    public SavingGoalResponse() {}

    public SavingGoalResponse(Long id, Long ownerId, Long walletId, String walletCurrencyCode,
                              String name, BigDecimal targetAmount, BigDecimal currentAmount,
                              LocalDate dueDate, boolean archived, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.walletId = walletId;
        this.walletCurrencyCode = walletCurrencyCode;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.dueDate = dueDate;
        this.archived = archived;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public String getWalletCurrencyCode() { return walletCurrencyCode; }
    public void setWalletCurrencyCode(String walletCurrencyCode) { this.walletCurrencyCode = walletCurrencyCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getTargetAmount() { return targetAmount; }
    public void setTargetAmount(BigDecimal targetAmount) { this.targetAmount = targetAmount; }

    public BigDecimal getCurrentAmount() { return currentAmount; }
    public void setCurrentAmount(BigDecimal currentAmount) { this.currentAmount = currentAmount; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}