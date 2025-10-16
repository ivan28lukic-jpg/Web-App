package group19.WebFinanceApp.controller.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public class AdminCurrencyResponse {
    private Long id;
    private String code;
    private String name;
    private BigDecimal valueVsEur;
    private Instant updatedAt;

    public AdminCurrencyResponse(Long id, String code, String name, BigDecimal valueVsEur, Instant updatedAt) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.valueVsEur = valueVsEur;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public BigDecimal getValueVsEur() { return valueVsEur; }
    public Instant getUpdatedAt() { return updatedAt; }
}