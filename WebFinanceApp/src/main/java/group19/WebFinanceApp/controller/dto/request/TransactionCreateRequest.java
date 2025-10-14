package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.Instant;

public class TransactionCreateRequest {

    @NotNull
    private Long walletId;

    @NotNull
    private Long categoryId;

    @NotNull
    @Positive
    private BigDecimal amount; // uvek pozitivna

    @Size(max = 255)
    private String description;

    @PastOrPresent
    private Instant occurredAt; // ako je null -> now()

    // ako praviš jednu stranu transfera “ručno”:
    private String transferId;

    public TransactionCreateRequest() {}

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public String getTransferId() { return transferId; }
    public void setTransferId(String transferId) { this.transferId = transferId; }
}