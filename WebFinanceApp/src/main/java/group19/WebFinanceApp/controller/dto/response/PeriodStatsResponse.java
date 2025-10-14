package group19.WebFinanceApp.controller.dto.response;

import java.math.BigDecimal;

public class PeriodStatsResponse {
    private String period;        // npr. "2025-39", "2025-09-26", "2025-09", "2025"
    private BigDecimal income;    // zbir INCOME
    private BigDecimal expense;   // zbir EXPENSE (pozitivan)
    private BigDecimal net;       // income - expense

    public PeriodStatsResponse() {}

    public PeriodStatsResponse(String period, BigDecimal income, BigDecimal expense, BigDecimal net) {
        this.period = period;
        this.income = income;
        this.expense = expense;
        this.net = net;
    }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }

    public BigDecimal getIncome() { return income; }
    public void setIncome(BigDecimal income) { this.income = income; }

    public BigDecimal getExpense() { return expense; }
    public void setExpense(BigDecimal expense) { this.expense = expense; }

    public BigDecimal getNet() { return net; }
    public void setNet(BigDecimal net) { this.net = net; }
}