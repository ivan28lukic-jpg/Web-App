package group19.WebFinanceApp.controller.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public class WalletResponse {
    private Long id;
    private Long ownerId;
    private String currencyCode;
    private String name;
    private boolean savings;
    private boolean archived;
    private BigDecimal balance;
    private Instant createdAt;

    public WalletResponse() {}

    public WalletResponse(Long id, Long ownerId, String currencyCode,
                          String name, boolean savings, boolean archived,
                          BigDecimal balance, Instant createdAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.currencyCode = currencyCode;
        this.name = name;
        this.savings = savings;
        this.archived = archived;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isSavings() { return savings; }
    public void setSavings(boolean savings) { this.savings = savings; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}