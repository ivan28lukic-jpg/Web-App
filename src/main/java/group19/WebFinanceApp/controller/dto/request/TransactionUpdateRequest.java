package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
import java.math.BigDecimal;
import java.time.Instant;

public class TransactionUpdateRequest {

    @NotNull
    private Long categoryId;

    @NotNull
    @Positive
    private BigDecimal amount;

    @Size(max = 255)
    private String description;

    @PastOrPresent
    private Instant occurredAt;

    private String transferId; // opcionalno

    public TransactionUpdateRequest() {}

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