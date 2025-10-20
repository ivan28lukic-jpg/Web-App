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

    @GetMapping("/search")
    public Page<TransactionResponse> adminSearch(
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) String ownerUsername,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CategoryType categoryType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "occurredAt,DESC") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));

        Instant effFrom = (fromDate != null)
                ? fromDate.atStartOfDay(ZoneOffset.UTC).toInstant()
                : from;
        Instant effTo = (toDate != null)
                ? toDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusNanos(1)
                : to;

        Page<Transaction> result = transactions.adminSearch(
                walletId,
                ownerUsername,
                categoryName,
                categoryId,
                categoryType,
                effFrom,
                effTo,
                minAmount,
                maxAmount,
                q,
                pageable
        );

        return result.map(this::toResponse);
    }

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
        TransactionResponse r = new TransactionResponse();
        r.setId(t.getId());
        r.setWalletId(t.getWallet().getId());
        r.setWalletName(t.getWallet().getName());
        r.setOwnerUsername(t.getWallet().getOwner() != null ? t.getWallet().getOwner().getUsername() : null);
        r.setCategoryId(t.getCategory() != null ? t.getCategory().getId() : null);
        r.setCategoryName(t.getCategory() != null ? t.getCategory().getName() : null);
        r.setCategoryType(t.getCategory() != null ? t.getCategory().getType() : null);
        r.setAmount(t.getAmount());
        r.setDescription(t.getDescription());
        r.setOccurredAt(t.getOccurredAt());
        r.setTransferId(t.getTransferId());
        r.setRecurringTemplateId(t.getRecurringTemplate() != null ? t.getRecurringTemplate().getId() : null);
        r.setRecurringInstanceId(t.getRecurringInstance() != null ? t.getRecurringInstance().getId() : null);
        return r;
    }
}