package group19.WebFinanceApp.controller.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public class SavingGoalProgressResponse {
    // osnovni podaci o cilju
    private Long goalId;
    private Long ownerId;
    private Long walletId;
    private String walletCurrencyCode;
    private String name;

    // sažetak napretka
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal remainingAmount;    // max(0, target - current)
    private BigDecimal progressPercent;    // 0..100 sa 2 decimale
    private LocalDate dueDate;             // može biti null
    private Long daysLeft;                 // može biti null ako dueDate==null
    private BigDecimal neededPerDay;       // koliko bi dnevno trebalo do roka (može biti null)

    // vremenska serija (za linijski graf)
    private Instant from;
    private Instant to;
    private List<SavingGoalProgressPoint> points;

    public SavingGoalProgressResponse(Long goalId, Long ownerId, Long walletId, String walletCurrencyCode, String name,
                                      BigDecimal targetAmount, BigDecimal currentAmount, BigDecimal remainingAmount,
                                      BigDecimal progressPercent, LocalDate dueDate, Long daysLeft, BigDecimal neededPerDay,
                                      Instant from, Instant to, List<SavingGoalProgressPoint> points) {
        this.goalId = goalId;
        this.ownerId = ownerId;
        this.walletId = walletId;
        this.walletCurrencyCode = walletCurrencyCode;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.remainingAmount = remainingAmount;
        this.progressPercent = progressPercent;
        this.dueDate = dueDate;
        this.daysLeft = daysLeft;
        this.neededPerDay = neededPerDay;
        this.from = from;
        this.to = to;
        this.points = points;
    }

    public Long getGoalId() { return goalId; }
    public Long getOwnerId() { return ownerId; }
    public Long getWalletId() { return walletId; }
    public String getWalletCurrencyCode() { return walletCurrencyCode; }
    public String getName() { return name; }
    public BigDecimal getTargetAmount() { return targetAmount; }
    public BigDecimal getCurrentAmount() { return currentAmount; }
    public BigDecimal getRemainingAmount() { return remainingAmount; }
    public BigDecimal getProgressPercent() { return progressPercent; }
    public LocalDate getDueDate() { return dueDate; }
    public Long getDaysLeft() { return daysLeft; }
    public BigDecimal getNeededPerDay() { return neededPerDay; }
    public Instant getFrom() { return from; }
    public Instant getTo() { return to; }
    public List<SavingGoalProgressPoint> getPoints() { return points; }
}