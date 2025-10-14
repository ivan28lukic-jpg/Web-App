// src/main/java/group19/WebFinanceApp/controller/dto/request/CurrencyRateUpdateRequest.java
package group19.WebFinanceApp.controller.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CurrencyRateUpdateRequest {
    @NotNull
    @DecimalMin(value = "0.000001")
    private BigDecimal valueVsEur;

    public BigDecimal getValueVsEur() { return valueVsEur; }
    public void setValueVsEur(BigDecimal valueVsEur) { this.valueVsEur = valueVsEur; }
}