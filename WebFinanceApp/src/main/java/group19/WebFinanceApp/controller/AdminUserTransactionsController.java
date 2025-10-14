package group19.WebFinanceApp.controller.admin;

import group19.WebFinanceApp.controller.dto.response.TransactionResponse;
import group19.WebFinanceApp.model.Transaction;
import group19.WebFinanceApp.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

@RestController
@RequestMapping("/api/admin/users/{userId}/transactions")
public class AdminUserTransactionsController {

    private final TransactionRepository transactions;

    public AdminUserTransactionsController(TransactionRepository transactions) {
        this.transactions = transactions;
    }

    /**
     * Pregled transakcija korisnika (admin), sa filterima i paginacijom.
     *
     * Query parametri (svi opcioni):
     *  - from,to : ISO-8601 Instant (npr. 2025-09-01T00:00:00Z)
     *  - categoryId : Long
     *  - min,max : BigDecimal
     *  - page,size : paginacija (default 0,10)
     *  - sort : po polju (default occurredAt,DESC). Dozvoljena polja: occurredAt, amount, id
     */
    @GetMapping
    public Page<TransactionResponse> list(
            @PathVariable Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal min,
            @RequestParam(required = false) BigDecimal max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "occurredAt,DESC") String sort
    ) {
        Sort springSort = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, springSort);

        return transactions.adminFindByOwnerFilters(
                        userId, from, to, categoryId, min, max, pageable)
                .map(this::toResponse);
    }

    // -- helpers --

    private Sort parseSort(String sort) {
        // očekujemo format "field,DESC" ili "field,ASC"
        try {
            String[] parts = sort.split(",", 2);
            String field = (parts.length > 0 ? parts[0] : "occurredAt");
            String dir = (parts.length > 1 ? parts[1] : "DESC");
            Sort.Direction direction = "ASC".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

            // dozvoli samo nekoliko bezbednih polja
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
        Long categoryId = t.getCategory() != null ? t.getCategory().getId() : null;
        String categoryName = t.getCategory() != null ? t.getCategory().getName() : null;
        var categoryType = t.getCategory() != null ? t.getCategory().getType() : null;

        Long recurringTemplateId = (t.getRecurringTemplate() != null) ? t.getRecurringTemplate().getId() : null;
        Long recurringInstanceId = (t.getRecurringInstance() != null) ? t.getRecurringInstance().getId() : null;

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