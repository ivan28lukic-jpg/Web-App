package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyUpdateRequest {

    @NotBlank
    @Size(max = 64)
    private String name;

    @NotNull
    @DecimalMin(value = "0.000001")
    @Digits(integer = 19, fraction = 6) // poravnato sa DB kolonom numeric(19,6)
    private BigDecimal valueVsEur;

    public CurrencyUpdateRequest() {}

    public String getName() { return name; }
    public void setName(String name) {
        this.name = (name == null ? null : name.trim());
    }

    public BigDecimal getValueVsEur() { return valueVsEur; }
    public void setValueVsEur(BigDecimal valueVsEur) {
        this.valueVsEur = (valueVsEur == null ? null : valueVsEur.setScale(6, RoundingMode.HALF_UP));
    }
}