package group19.WebFinanceApp.controller.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public class CurrencyFetchResponse {
    private String code;
    private BigDecimal suggestedValueVsEur;
    private String source;
    private Instant fetchedAt;

    public CurrencyFetchResponse(String code, BigDecimal suggestedValueVsEur, String source, Instant fetchedAt) {
        this.code = code;
        this.suggestedValueVsEur = suggestedValueVsEur;
        this.source = source;
        this.fetchedAt = fetchedAt;
    }

    public String getCode() { return code; }
    public BigDecimal getSuggestedValueVsEur() { return suggestedValueVsEur; }
    public String getSource() { return source; }
    public Instant getFetchedAt() { return fetchedAt; }
}