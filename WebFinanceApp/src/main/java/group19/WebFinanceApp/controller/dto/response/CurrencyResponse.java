package group19.WebFinanceApp.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

public class CurrencyResponse {

    private String code;
    private String name;
    private BigDecimal valueVsEur; // EUR za 1 jedinicu valute
    private Instant updatedAt;

    public CurrencyResponse() {}

    public CurrencyResponse(String code, String name, BigDecimal valueVsEur, Instant updatedAt) {
        setCode(code);
        setName(name);
        setValueVsEur(valueVsEur);
        this.updatedAt = updatedAt;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = (code == null ? null : code.trim().toUpperCase()); }

    public String getName() { return name; }
    public void setName(String name) { this.name = (name == null ? null : name.trim()); }

    public BigDecimal getValueVsEur() { return valueVsEur; }
    public void setValueVsEur(BigDecimal valueVsEur) {
        this.valueVsEur = (valueVsEur == null ? null : valueVsEur.setScale(6, RoundingMode.HALF_UP));
    }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    // --- izvedena polja koja olakšavaju rad na klijentu ---

    /** Koliko jedinica ove valute ide za 1 EUR (npr. za RSD ~117.000000). */
    @JsonProperty("unitsPerEur")
    public BigDecimal getUnitsPerEur() {
        if (valueVsEur == null || valueVsEur.compareTo(BigDecimal.ZERO) == 0) return null;
        return BigDecimal.ONE.divide(valueVsEur, 6, RoundingMode.HALF_UP);
    }

    /** Da li je ova valuta bazna (EUR). */
    @JsonProperty("isBase")
    public boolean isBase() {
        return "EUR".equalsIgnoreCase(code);
    }
}