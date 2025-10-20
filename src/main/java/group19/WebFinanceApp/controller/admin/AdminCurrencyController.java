package group19.WebFinanceApp.controller.admin;

import group19.WebFinanceApp.controller.dto.request.CurrencyCreateRequest;
import group19.WebFinanceApp.controller.dto.request.CurrencyUpdateRequest;
import group19.WebFinanceApp.controller.dto.response.AdminCurrencyResponse;
import group19.WebFinanceApp.controller.dto.response.CurrencyFetchResponse;
import group19.WebFinanceApp.model.Currency;
import group19.WebFinanceApp.repository.CurrencyRepository;
import group19.WebFinanceApp.service.CurrencyRateClient;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/currencies")
public class AdminCurrencyController {

    private final CurrencyRepository currencies;
    private final CurrencyRateClient rateClient;

    public AdminCurrencyController(CurrencyRepository currencies, CurrencyRateClient rateClient) {
        this.currencies = currencies;
        this.rateClient = rateClient;
    }

    // ---- BASIC CRUD ----

    @GetMapping
    public List<AdminCurrencyResponse> list() {
        return currencies.findAll().stream().map(this::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<AdminCurrencyResponse> create(@Valid @RequestBody CurrencyCreateRequest in) {
        String code = in.getCode().toUpperCase();
        if (currencies.existsByCodeIgnoreCase(code)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Currency c = new Currency();
        c.setCode(code);
        c.setName(in.getName());
        c.setValueVsEur(in.getValueVsEur());
        c.setUpdatedAt(Instant.now());

        c = currencies.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(c));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminCurrencyResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody CurrencyUpdateRequest in) {
        return currencies.findById(id)
                .map(c -> {
                    if (in.getName() != null) c.setName(in.getName());
                    if (in.getValueVsEur() != null) c.setValueVsEur(in.getValueVsEur());
                    c.setUpdatedAt(Instant.now());
                    Currency saved = currencies.save(c);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ---- API FETCH ----

    // Samo predlog kursa, NE upisuje u bazu.
    @GetMapping("/fetch")
    public ResponseEntity<?> fetch(@RequestParam String code) {
        var res = rateClient.fetchRateEurTo(code);
        if (res == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "rate_not_available",
                            "message", "No EUR->" + code.toUpperCase() + " rate from frankfurter.app"
                    ));
        }
        return ResponseEntity.ok(
                new CurrencyFetchResponse(res.code(), res.valueVsEur(), res.source(), res.fetchedAt())
        );
    }

    /** Povuci kurs i upiši ga za postojeću valutu. */
    @PatchMapping("/{id}/refresh")
    public ResponseEntity<?> refresh(@PathVariable Long id) {
        return currencies.findById(id)
                .map(c -> {
                    var fetched = rateClient.fetchRateEurTo(c.getCode());
                    if (fetched == null) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of(
                                        "error", "rate_not_available",
                                        "message", "No EUR->" + c.getCode() + " rate from frankfurter.app"
                                ));
                    }
                    c.setValueVsEur(fetched.valueVsEur());
                    c.setUpdatedAt(Instant.now());
                    Currency saved = currencies.save(c);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!currencies.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        currencies.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // ---- helpers ----
    private AdminCurrencyResponse toResponse(Currency c) {
        return new AdminCurrencyResponse(
                c.getId(), c.getCode(), c.getName(), c.getValueVsEur(), c.getUpdatedAt()
        );
    }
}