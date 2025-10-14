package group19.WebFinanceApp.controller.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public class TransferResponse {

    private String transferId;

    private Long fromWalletId;
    private String fromWalletName;

    private Long toWalletId;
    private String toWalletName;

    private BigDecimal amount;
    private String description;     // opis (tipično sa "expense" strane)
    private Instant occurredAt;

    private Long outTransactionId;  // EXPENSE transakcija
    private Long inTransactionId;   // INCOME  transakcija

    public TransferResponse() {}

    public TransferResponse(String transferId,
                            Long fromWalletId, String fromWalletName,
                            Long toWalletId, String toWalletName,
                            BigDecimal amount, String description, Instant occurredAt,
                            Long outTransactionId, Long inTransactionId) {
        this.transferId = transferId;
        this.fromWalletId = fromWalletId;
        this.fromWalletName = fromWalletName;
        this.toWalletId = toWalletId;
        this.toWalletName = toWalletName;
        this.amount = amount;
        this.description = description;
        this.occurredAt = occurredAt;
        this.outTransactionId = outTransactionId;
        this.inTransactionId = inTransactionId;
    }

    public TransferResponse(String transferId,
                            TransactionResponse outTxn,
                            TransactionResponse inTxn) {
        this.transferId = transferId;

        if (outTxn != null) {
            this.fromWalletId = outTxn.getWalletId();
            this.fromWalletName = null;
            this.amount = outTxn.getAmount();
            this.description = outTxn.getDescription();
            this.occurredAt = outTxn.getOccurredAt();
            this.outTransactionId = outTxn.getId();
        }
        if (inTxn != null) {
            this.toWalletId = inTxn.getWalletId();
            this.toWalletName = null;
            this.inTransactionId = inTxn.getId();
        }
    }

    public String getTransferId() { return transferId; }
    public void setTransferId(String transferId) { this.transferId = transferId; }

    public Long getFromWalletId() { return fromWalletId; }
    public void setFromWalletId(Long fromWalletId) { this.fromWalletId = fromWalletId; }

    public String getFromWalletName() { return fromWalletName; }
    public void setFromWalletName(String fromWalletName) { this.fromWalletName = fromWalletName; }

    public Long getToWalletId() { return toWalletId; }
    public void setToWalletId(Long toWalletId) { this.toWalletId = toWalletId; }

    public String getToWalletName() { return toWalletName; }
    public void setToWalletName(String toWalletName) { this.toWalletName = toWalletName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public Long getOutTransactionId() { return outTransactionId; }
    public void setOutTransactionId(Long outTransactionId) { this.outTransactionId = outTransactionId; }

    public Long getInTransactionId() { return inTransactionId; }
    public void setInTransactionId(Long inTransactionId) { this.inTransactionId = inTransactionId; }
}