package group19.WebFinanceApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

// ⬇⬇⬇ ako su ovi u drugom paketu (npr. model.recurring), promeni import-e ⬇⬇⬇
import group19.WebFinanceApp.model.RecurringTemplate;
import group19.WebFinanceApp.model.RecurringInstance;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category; // (INCOME/EXPENSE) određuje smer

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount; // uvek pozitivna vrednost

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Instant occurredAt;

    // zajednički ID obe transfer transakcije (ako je transfer)
    @Column(length = 36)
    private String transferId;

    // 🔹 novo: veza na recurring template (ako je nastala iz šablona)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_template_id")
    private RecurringTemplate recurringTemplate;

    // 🔹 novo: veza na konkretno izvršenje (instancu) recurrence-a
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recurring_instance_id")
    private RecurringInstance recurringInstance;

    public Transaction() {}

    @PrePersist
    protected void onCreate() {
        if (occurredAt == null) occurredAt = Instant.now();
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Wallet getWallet() { return wallet; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Instant getOccurredAt() { return occurredAt; }
    public void setOccurredAt(Instant occurredAt) { this.occurredAt = occurredAt; }

    public String getTransferId() { return transferId; }
    public void setTransferId(String transferId) { this.transferId = transferId; }

    public RecurringTemplate getRecurringTemplate() { return recurringTemplate; }
    public void setRecurringTemplate(RecurringTemplate recurringTemplate) { this.recurringTemplate = recurringTemplate; }

    public RecurringInstance getRecurringInstance() { return recurringInstance; }
    public void setRecurringInstance(RecurringInstance recurringInstance) { this.recurringInstance = recurringInstance; }
}