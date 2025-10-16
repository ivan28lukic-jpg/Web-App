package group19.WebFinanceApp.controller.dto.request;

import group19.WebFinanceApp.model.RecurringFrequency;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class RecurringTemplateUpdateRequest {

    @NotBlank private String name;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    private String descriptionTemplate;

    @NotNull private RecurringFrequency frequency;
    @Min(1)  private int interval = 1;

    @NotNull private LocalDate startDate;
    private LocalDate endDate;

    @NotNull private Long walletId;
    @NotNull private Long categoryId;

    private boolean active;

    public RecurringTemplateUpdateRequest() {}

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

    public Long getWalletId() { return walletId; }
    public void setWalletId(Long walletId) { this.walletId = walletId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}