package group19.WebFinanceApp.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
        name = "recurring_instances",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_recurring_template_period", columnNames = {"template_id", "period_key"})
        }
)
public class RecurringInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // referenca na template
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "template_id", nullable = false)
    private RecurringTemplate template;

    // npr. "2025-10-01" (Localized ISO datum)
    @Column(name = "period_key", nullable = false, length = 16)
    private String periodKey;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    public RecurringInstance() {}

    public RecurringInstance(RecurringTemplate template, String periodKey, Instant executedAt) {
        this.template = template;
        this.periodKey = periodKey;
        this.executedAt = executedAt;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public RecurringTemplate getTemplate() { return template; }
    public void setTemplate(RecurringTemplate template) { this.template = template; }

    public String getPeriodKey() { return periodKey; }
    public void setPeriodKey(String periodKey) { this.periodKey = periodKey; }

    public Instant getExecutedAt() { return executedAt; }
    public void setExecutedAt(Instant executedAt) { this.executedAt = executedAt; }
}