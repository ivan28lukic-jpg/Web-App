package group19.WebFinanceApp.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import group19.WebFinanceApp.model.RecurringFrequency;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class RecurringTemplateResponse {

    private Long id;
    private Long ownerId;
    private Long walletId;
    private Long categoryId;
    private String walletCurrencyCode;

    private String name;
    private BigDecimal amount;
    private String descriptionTemplate;

    private RecurringFrequency frequency;
    private int interval;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextRunDate;

    private boolean active;

    private Instant createdAt;
    private Instant updatedAt;

    public RecurringTemplateResponse() {}

    public RecurringTemplateResponse(Long id, Long ownerId, Long walletId, Long categoryId, String walletCurrencyCode,
                                     String name, BigDecimal amount, String descriptionTemplate,
                                     RecurringFrequency frequency, int interval,
                                     LocalDate startDate, LocalDate endDate, LocalDate nextRunDate,
                                     boolean active, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.walletId = walletId;
        this.categoryId = categoryId;
        this.walletCurrencyCode = walletCurrencyCode;
        this.name = name;
        this.amount = amount;
        this.descriptionTemplate = descriptionTemplate;
        this.frequency = frequency;
        this.interval = interval;
        this.startDate = startDate;
        this.endDate = endDate;
        this.nextRunDate = nextRunDate;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // getters/setters
    @JsonProperty("templateId")
    public Long getId() { return id; }

    @JsonProperty("templateId")
    public void setId(Long id) { this.id = id; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getWalletCurrencyCode() { return walletCurrencyCode; }
    public void setWalletCurrencyCode(String walletCurrencyCode) { this.walletCurrencyCode = walletCurrencyCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getDescriptionTemplate() { return descriptionTemplate; }
    public void setDescriptionTemplate(String descriptionTemplate) { this.descriptionTemplate = descriptionTemplate; }

    public RecurringFrequency getFrequency() { return frequency; }
    public void setFrequency(RecurringFrequency frequency) { this.frequency = frequency; }

    public int getInterval() { return interval; }
    public void setInterval(int interval) { this.interval = interval; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public LocalDate getNextRunDate() { return nextRunDate; }
    public void setNextRunDate(LocalDate nextRunDate) { this.nextRunDate = nextRunDate; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}