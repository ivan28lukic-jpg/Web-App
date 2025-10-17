package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class WalletCreateRequest {

    @NotBlank
    private String name;

    @NotBlank
    private String currencyCode;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal balance;

    private boolean savings;

    public WalletCreateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public boolean isSavings() { return savings; }
    public void setSavings(boolean savings) { this.savings = savings; }
}