package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * PUT update zahteva da pošalješ kompletan payload (kao za create),
 * a opcionalno možeš da promeniš i sam transferId preko newTransferId.
 */
public class TransferUpdateRequest {

    @NotNull
    private Long fromWalletId;

    @NotNull
    private Long toWalletId;

    @NotNull
    private Long outCategoryId; // mora biti EXPENSE

    @NotNull
    private Long inCategoryId;  // mora biti INCOME

    @NotNull @Positive
    private BigDecimal amount;

    @Size(max = 255)
    private String description;

    @PastOrPresent
    private Instant occurredAt;

    // ako je null ili prazan - ostaje stari transferId
    private String newTransferId;

    public TransferUpdateRequest() {}

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

    public String getNewTransferId() { return newTransferId; }
    public void setNewTransferId(String newTransferId) { this.newTransferId = newTransferId; }
}