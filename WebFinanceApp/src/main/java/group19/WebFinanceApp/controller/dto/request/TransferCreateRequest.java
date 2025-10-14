package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;

public class TransferCreateRequest {

    @NotNull
    private Long fromWalletId;

    @NotNull
    private Long toWalletId;

    // eksplicitno navodimo kategorije da ne uvodimo lookup po imenu
    @NotNull
    private Long outCategoryId; // EXPENSE kategorija (npr. "Transfer Out")

    @NotNull
    private Long inCategoryId;  // INCOME  kategorija (npr. "Transfer In")

    @NotNull
    @Positive
    private BigDecimal amount;

    @Size(max = 255)
    private String description;

    @PastOrPresent
    private Instant occurredAt;

    // dozvoljavamo i custom transferId; ako je null generisaćemo UUID
    private String transferId;

    public TransferCreateRequest() {}

    public Long getFromWalletId() { return fromWalletId; }
    public void setFromWalletId(Long fromWalletId) { this.fromWalletId = fromWalletId; }

    public Long getToWalletId() { return toWalletId; }
    public void setToWalletId(Long toWalletId) { this.toWalletId = toWalletId; }

    public Long getOutCategoryId() { return outCategoryId; }
    public void setOutCategoryId(Long outCategoryId) { this.outCategoryId = outCategoryId; }

    public Long getInCategoryId() { return inCategoryId; }
    public void setInCategoryId(Long inCategoryId) { this.inCategoryId = inCategoryId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public String getTransferId() { return transferId; }
    public void setTransferId(String transferId) { this.transferId = transferId; }
}