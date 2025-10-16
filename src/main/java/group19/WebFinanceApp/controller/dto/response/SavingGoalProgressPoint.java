package group19.WebFinanceApp.controller.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public class SavingGoalProgressPoint {
    private Instant occurredAt;
    private BigDecimal amount;          // iznos pojedinačne uplate (IN na goal wallet)
    private BigDecimal cumulativeTotal; // kumulativno do tog trenutka

    public SavingGoalProgressPoint(Instant occurredAt, BigDecimal amount, BigDecimal cumulativeTotal) {
        this.occurredAt = occurredAt;
        this.amount = amount;
        this.cumulativeTotal = cumulativeTotal;
    }

    public Instant getOccurredAt() { return occurredAt; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getCumulativeTotal() { return cumulativeTotal; }
}