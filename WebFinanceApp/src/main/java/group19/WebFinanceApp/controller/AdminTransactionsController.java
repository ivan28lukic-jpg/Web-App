package group19.WebFinanceApp.controller.admin;

import group19.WebFinanceApp.controller.dto.response.TransactionResponse;
import group19.WebFinanceApp.model.CategoryType;
import group19.WebFinanceApp.model.Transaction;
import group19.WebFinanceApp.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/transactions")
public class AdminTransactionsController {

    private final TransactionRepository transactions;

    public AdminTransactionsController(TransactionRepository transactions) {
        this.transactions = transactions;
    }

    /**
     * Monitoring svih transakcija u sistemu (admin).
     *
     * Datumski filteri – možeš koristiti:
     *  - 'from' / 'to' kao ISO Instant (npr. 2025-09-01T00:00:00Z), ili
     *  - 'fromDate' / 'toDate' kao YYYY-MM-DD (npr. 2025-09-01). Ako su ovi zadati,
     *    imaju prednost i automatski se prevode na ceo dan u UTC.
     */
    @GetMapping
    public Page<TransactionResponse> list(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CategoryType categoryType,

            // stari način (ostaje podržan)
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,

            // NOVO: čisti datum
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,

            @RequestParam(required = false) BigDecimal min,
            @RequestParam(required = false) BigDecimal max,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "occurredAt,DESC") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));

        // ako su zadati fromDate/toDate, imaju prednost nad from/to
        Instant effFrom = (fromDate != null)
                ? fromDate.atStartOfDay(ZoneOffset.UTC).toInstant()
                : from;

        Instant effTo = (toDate != null)
                ? toDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusNanos(1) // inclusive kraj dana
                : to;

        Page<Transaction> result = transactions.search(
                walletId,
                ownerId,
                categoryId,
                categoryType,
                effFrom,
                effTo,
                min,
                max,
                q,
                pageable
        );

        return result.map(this::toResponse);
    }

    // --- helpers ---

    private Sort parseSort(String sort) {
        try {
            String[] parts = sort.split(",", 2);
            String field = (parts.length > 0 ? parts[0].trim() : "occurredAt");
            String dir = (parts.length > 1 ? parts[1].trim() : "DESC");
            Sort.Direction direction = "ASC".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

            if (!Objects.equals(field, "occurredAt") &&
                    !Objects.equals(field, "amount") &&
                    !Objects.equals(field, "id")) {
                field = "occurredAt";
            }
            return Sort.by(direction, field);
        } catch (Exception ignore) {
            return Sort.by(Sort.Direction.DESC, "occurredAt");
        }
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getWallet().getId(),
                (t.getCategory() != null ? t.getCategory().getId() : null),
                (t.getCategory() != null ? t.getCategory().getName() : null),
                (t.getCategory() != null ? t.getCategory().getType() : null),
                t.getAmount(),
                t.getDescription(),
                t.getOccurredAt(),
                t.getTransferId(),
                (t.getRecurringTemplate() != null ? t.getRecurringTemplate().getId() : null),
                (t.getRecurringInstance() != null ? t.getRecurringInstance().getId() : null)
        );
    }
}