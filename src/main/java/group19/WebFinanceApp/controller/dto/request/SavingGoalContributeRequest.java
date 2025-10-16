package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Uplata na cilj štednje = transfer sa "fromWalletId" -> goal.wallet.
 * amount je u valuti FROM novčanika.
 */
public class SavingGoalContributeRequest {

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private Long fromWalletId; // IZVORNI novčanik

    private String description;
    private Instant occurredAt;

    // opciono: ako ne proslediš, koristiće se fallback "Transfer Out"/"Transfer In"
    private Long outCategoryId;
    private Long inCategoryId;

    public SavingGoalContributeRequest() {}

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Long getFromWalletId() { return fromWalletId; }
    public void setFromWalletId(Long fromWalletId) { this.fromWalletId = fromWalletId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public Long getOutCategoryId() { return outCategoryId; }
    public void setOutCategoryId(Long outCategoryId) { this.outCategoryId = outCategoryId; }

    public Long getInCategoryId() { return inCategoryId; }
    public void setInCategoryId(Long inCategoryId) { this.inCategoryId = inCategoryId; }
}