package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyCreateRequest {

    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "Code must be exactly 3 uppercase letters")
    private String code;

    @NotBlank
    @Size(max = 64, message = "Name max length is 64")
    private String name;

    @NotNull
    @DecimalMin(value = "0.000001", message = "valueVsEur must be > 0")
    @Digits(integer = 19, fraction = 6) // poravnato sa DB kolonom numeric(19,6)
    private BigDecimal valueVsEur;

    public CurrencyCreateRequest() {}

    public String getCode() { return code; }
    public void setCode(String code) {
        this.code = (code == null ? null : code.trim().toUpperCase());
    }

    public String getName() { return name; }
    public void setName(String name) {
        this.name = (name == null ? null : name.trim());
    }

    public BigDecimal getValueVsEur() { return valueVsEur; }
    public void setValueVsEur(BigDecimal valueVsEur) {
        // normalizuj na 6 decimala da ne puca na DB numeric(19,6)
        this.valueVsEur = (valueVsEur == null ? null : valueVsEur.setScale(6, RoundingMode.HALF_UP));
    }
}