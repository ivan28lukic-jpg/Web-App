package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Povlačenje sa cilja štednje = transfer sa goal.wallet -> toWalletId.
 * amount je u valuti ciljnog (goal) novčanika (tj. wallet-a na koji je goal vezan).
 */
public class SavingGoalWithdrawRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private Long toWalletId; // ODREDIŠNI novčanik

    private String description;
    private Instant occurredAt;

    // opciono: ako ne proslediš, koristi se fallback "Transfer Out"/"Transfer In"
    private Long outCategoryId; // za OUT na goal.wallet
    private Long inCategoryId;  // za IN na toWalletId

    public SavingGoalWithdrawRequest() {}

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Long getToWalletId() { return toWalletId; }
    public void setToWalletId(Long toWalletId) { this.toWalletId = toWalletId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public Long getOutCategoryId() { return outCategoryId; }
    public void setOutCategoryId(Long outCategoryId) { this.outCategoryId = outCategoryId; }

    public Long getInCategoryId() { return inCategoryId; }
    public void setInCategoryId(Long inCategoryId) { this.inCategoryId = inCategoryId; }
}