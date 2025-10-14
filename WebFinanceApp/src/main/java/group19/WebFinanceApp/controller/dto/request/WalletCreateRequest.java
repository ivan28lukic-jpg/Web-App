package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class WalletCreateRequest {

    @NotNull
    private Long ownerId;

    @NotBlank
    private String name;

    @NotBlank
    private String currencyCode;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal initialBalance;

    // NOVO: po difoltu false ako ne pošalješ
    private boolean savings;

    public WalletCreateRequest() {}

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }

    public boolean isSavings() { return savings; }
    public void setSavings(boolean savings) { this.savings = savings; }
}