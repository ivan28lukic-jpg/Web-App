package group19.WebFinanceApp.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

/**
 * Jednostavan klijent za frankfurter.dev API (EUR kao baza).
 * Primer: GET https://api.frankfurter.dev/latest?from=EUR&to=USD
 */
@Component
public class CurrencyRateClient {

    private final RestClient http;
    private final String baseUrl = "https://api.frankfurter.app";

    public CurrencyRateClient(RestClient.Builder restClientBuilder) {
        this.http = restClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Vrati kurs 1 EUR -> code (npr. USD), ili null ako ne postoji.
     */
    public FetchResult fetchRateEurTo(String code) {
        String upper = code.toUpperCase();
        try {
            FrankfurterLatest res = http.get()
                    .uri(uri -> uri.path("/latest")
                            .queryParam("from", "EUR")
                            .queryParam("to", upper)
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, resp) -> { throw new RuntimeException("Fetch error: " + resp.getStatusCode()); })
                    .body(FrankfurterLatest.class);

            if (res == null || res.rates == null) return null;
            BigDecimal rate = res.rates.get(upper);
            if (rate == null) return null;
            return new FetchResult(upper, rate, "frankfurter.dev", Instant.now());
        } catch (Exception ex) {
            // U produkciji bi se ovde logovalo
            return null;
        }
    }

    // --- DTO-i za JSON mapiranje ---

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class FrankfurterLatest {
        public String base;
        public String date;
        public Map<String, BigDecimal> rates;

        @JsonProperty("time_last_updated")
        public Long timeLastUpdated;
    }

    public record FetchResult(String code, BigDecimal valueVsEur, String source, Instant fetchedAt) {}
}