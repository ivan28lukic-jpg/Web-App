package group19.WebFinanceApp.controller;

import group19.WebFinanceApp.controller.dto.request.CurrencyCreateRequest;
import group19.WebFinanceApp.controller.dto.request.CurrencyUpdateRequest;
import group19.WebFinanceApp.controller.dto.response.CurrencyResponse;
import group19.WebFinanceApp.model.Currency;
import group19.WebFinanceApp.repository.CurrencyRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final CurrencyRepository currencies;

    public CurrencyController(CurrencyRepository currencies) {
        this.currencies = currencies;
    }

    // ===== LIST (bez paginacije) - sortirano po code ASC =====
    @GetMapping
    public List<CurrencyResponse> all() {
        return currencies.findAllByOrderByCodeAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ===== LIST paged: ?page=0&size=20&sort=code,asc =====
    @GetMapping("/paged")
    public Page<CurrencyResponse> paged(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "code,asc") String sort
    ) {
        Sort s = Sort.by("code").ascending();
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",", 2);
            String field = parts[0].trim();
            String dir   = (parts.length > 1 ? parts[1].trim() : "asc");
            Sort.Direction direction = "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;
            if ("code".equalsIgnoreCase(field) || "name".equalsIgnoreCase(field) || "updatedAt".equalsIgnoreCase(field)) {
                s = Sort.by(direction, field);
            }
        }
        Pageable pageable = PageRequest.of(page, size, s);
        return currencies.findAll(pageable).map(this::toResponse);
    }

    // ===== GET by code (EUR, RSD...) =====
    @GetMapping("/{code}")
    public ResponseEntity<CurrencyResponse> byCode(@PathVariable String code) {
        return currencies.findByCodeIgnoreCase(code)
                .map(c -> ResponseEntity.ok(toResponse(c)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ===== CREATE =====
    @PostMapping
    public ResponseEntity<CurrencyResponse> create(@Valid @RequestBody CurrencyCreateRequest in) {
        String code = in.getCode().toUpperCase();
        if (currencies.existsByCodeIgnoreCase(code)) {
            // 409 Conflict – duplikat koda
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
        Currency c = new Currency(code, in.getName(), in.getValueVsEur());
        c.setUpdatedAt(Instant.now());
        c = currencies.save(c);
        return ResponseEntity.ok(toResponse(c));
    }

    // ===== UPDATE name/rate (code je path param i ne menja se) =====
    @PutMapping("/{code}")
    public ResponseEntity<CurrencyResponse> update(@PathVariable String code,
                                                   @Valid @RequestBody CurrencyUpdateRequest in) {
        return currencies.findByCodeIgnoreCase(code)
                .map(c -> {
                    c.setName(in.getName());
                    c.setValueVsEur(in.getValueVsEur());
                    c.setUpdatedAt(Instant.now());
                    currencies.save(c);
                    return ResponseEntity.ok(toResponse(c));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ===== PATCH rate – brzo ažuriranje samo kursa =====
    // primer: PATCH /api/currencies/USD/rate?valueVsEur=0.91875
    @PatchMapping("/{code}/rate")
    public ResponseEntity<CurrencyResponse> patchRate(@PathVariable String code,
                                                      @RequestParam BigDecimal valueVsEur) {
        return currencies.findByCodeIgnoreCase(code)
                .map(c -> {
                    c.setValueVsEur(valueVsEur);
                    c.setUpdatedAt(Instant.now());
                    currencies.save(c);
                    return ResponseEntity.ok(toResponse(c));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // ===== DELETE – 409 ako je vezana FK (npr. postoji wallet sa tom valutom) =====
    @DeleteMapping("/{code}")
    public ResponseEntity<Void> delete(@PathVariable String code) {
        var opt = currencies.findByCodeIgnoreCase(code);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        try {
            currencies.delete(opt.get());
            return ResponseEntity.noContent().build();
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    private CurrencyResponse toResponse(Currency c) {
        return new CurrencyResponse(
                c.getCode(),
                c.getName(),
                c.getValueVsEur(),
                c.getUpdatedAt()
        );
    }
}