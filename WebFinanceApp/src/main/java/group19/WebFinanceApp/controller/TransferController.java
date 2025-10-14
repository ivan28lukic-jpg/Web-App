package group19.WebFinanceApp.controller;

import group19.WebFinanceApp.controller.dto.request.TransferCreateRequest;
import group19.WebFinanceApp.controller.dto.response.TransactionResponse;
import group19.WebFinanceApp.controller.dto.response.TransferResponse;
import group19.WebFinanceApp.model.Category;
import group19.WebFinanceApp.model.CategoryType;
import group19.WebFinanceApp.model.Transaction;
import group19.WebFinanceApp.model.Wallet;
import group19.WebFinanceApp.model.Currency;
import group19.WebFinanceApp.repository.CategoryRepository;
import group19.WebFinanceApp.repository.TransactionRepository;
import group19.WebFinanceApp.repository.WalletRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import java.time.ZoneOffset;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransactionRepository transactions;
    private final WalletRepository wallets;
    private final CategoryRepository categories;

    public TransferController(TransactionRepository transactions,
                              WalletRepository wallets,
                              CategoryRepository categories) {
        this.transactions = transactions;
        this.wallets = wallets;
        this.categories = categories;
    }

    // ========== 2) GET po transferId (GET /api/transfers/{transferId}) ==========
    @GetMapping("/{transferId}")
    public ResponseEntity<List<TransactionResponse>> getByTransferId(@PathVariable String transferId) {
        if (!transactions.existsByTransferId(transferId)) {
            return ResponseEntity.notFound().build();
        }
        var list = transactions.findByTransferIdOrderByOccurredAtAsc(transferId)
                .stream()
                .map(this::toTransactionResponse)
                .toList();
        return ResponseEntity.ok(list);
    }

    // ========== 3) LIST sa filterima (GET /api/transfers/paged) ==========
    @GetMapping("/paged")
    public ResponseEntity<Page<TransferResponse>> search(
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to") String toStr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "occurredAt,desc") String sort
    ) {
        Pageable pageable = buildPageable(page, size, sort);
        Instant from = parseFrom(fromStr);
        Instant to   = parseTo(toStr);

        // Uzimamo po 1 zapis po transferu – EXPENSE strana kao "representative"
        Page<Transaction> outs = transactions.findTransferOutsInvolvingWallet(walletId, from, to, pageable);

        Page<TransferResponse> mapped = outs.map(out -> {
            var in = transactions.findFirstByTransferIdAndCategory_Type(out.getTransferId(), CategoryType.INCOME)
                    .orElse(null);
            return toTransferResponse(out, in);
        });

        return ResponseEntity.ok(mapped);
    }

    // ---- Helpers ----

    // Konverzija iznosa između valuta preko valueVsEur (vrednost 1 jedinice valute izražena u EUR)
    private BigDecimal convertAmount(BigDecimal amountSrc, Currency fromCur, Currency toCur) {
        if (amountSrc == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (fromCur == null || toCur == null) return amountSrc.setScale(2, RoundingMode.HALF_UP);

        if (fromCur.getId().equals(toCur.getId())) {
            return amountSrc.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal fromVsEur = fromCur.getValueVsEur();
        BigDecimal toVsEur   = toCur.getValueVsEur();
        if (fromVsEur == null || toVsEur == null || fromVsEur.signum() <= 0 || toVsEur.signum() <= 0) {
            // ako su kursevi loši, vrati izvorni iznos (ili baci 409 ako želiš strože)
            return amountSrc.setScale(2, RoundingMode.HALF_UP);
        }

        // amountDst = amountSrc * (from.valueVsEur / to.valueVsEur)
        BigDecimal eurValue = amountSrc.multiply(fromVsEur); // u EUR
        BigDecimal dst = eurValue.divide(toVsEur, 8, RoundingMode.HALF_UP);
        return dst.setScale(2, RoundingMode.HALF_UP);
    }

    private Pageable buildPageable(int page, int size, String sortParam) {
        Sort sort = Sort.by("occurredAt").descending();
        if (sortParam != null && !sortParam.isBlank()) {
            String[] parts = sortParam.split(",", 2);
            String field = parts[0].trim();
            String dir = (parts.length > 1 ? parts[1].trim() : "desc");
            Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
            if ("occurredAt".equals(field)) {
                sort = Sort.by(direction, "occurredAt");
            }
        }
        return PageRequest.of(page, size, sort);
    }

    // mapiranje za stare testove (TransactionResponse)
    private TransactionResponse toTransactionResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getWallet().getId(),
                t.getCategory().getId(),
                t.getCategory().getName(),
                t.getCategory().getType(),  // CategoryType
                t.getAmount(),
                t.getDescription(),
                t.getOccurredAt(),
                t.getTransferId()
        );
    }

    // mapiranje za novi listing (TransferResponse "flat")
    private TransferResponse toTransferResponse(Transaction out, Transaction in) {
        Long toWalletId   = (in != null && in.getWallet() != null) ? in.getWallet().getId()   : null;
        String toWalletNm = (in != null && in.getWallet() != null) ? in.getWallet().getName() : null;

        return new TransferResponse(
                out.getTransferId(),
                out.getWallet().getId(),
                out.getWallet().getName(),
                toWalletId,
                toWalletNm,
                out.getAmount(),          // prikazujemo "out" iznos (izvorna valuta)
                out.getDescription(),
                out.getOccurredAt(),
                out.getId(),
                (in != null ? in.getId() : null)
        );
    }

    // ========== 4) LIST bez Page objekta (GET /api/transfers) ==========
    @GetMapping
    public ResponseEntity<List<TransferResponse>> listTransfers(
            @RequestParam(required = false) Long fromWalletId,
            @RequestParam(required = false) Long toWalletId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to")   String toStr,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "occurredAt,desc") String sort
    ) {
        Instant from = parseFrom(fromStr);
        Instant to   = parseTo(toStr);

        // 1) Učitaj sve transfer transakcije (one koje imaju transferId)
        List<Transaction> all = transactions.findByTransferIdIsNotNull();

        // 2) Vremenski filter (ako je zadat)
        all = all.stream()
                .filter(t -> from == null || !t.getOccurredAt().isBefore(from))
                .filter(t -> to   == null || !t.getOccurredAt().isAfter(to))
                .toList();

        // 3) Grupisanje po transferId
        var byTransfer = all.stream()
                .collect(java.util.stream.Collectors.groupingBy(Transaction::getTransferId));

        // 4) Wallet/owner filteri NAD GRUPAMA
        var filteredGroups = byTransfer.entrySet().stream().filter(e -> {
            var list = e.getValue();

            boolean passFrom  = (fromWalletId == null) ||
                    list.stream().anyMatch(t -> t.getWallet().getId().equals(fromWalletId));

            boolean passTo    = (toWalletId == null) ||
                    list.stream().anyMatch(t -> t.getWallet().getId().equals(toWalletId));

            boolean passOwner = (ownerId == null) ||
                    list.stream().anyMatch(t -> t.getWallet().getOwner().getId().equals(ownerId));

            return passFrom && passTo && passOwner;
        }).toList();

        // 5) Sortiranje (occurredAt,[asc|desc])
        String[] sortParts = sort.split(",", 2);
        String prop = sortParts[0].trim();
        boolean desc = sortParts.length > 1 && "desc".equalsIgnoreCase(sortParts[1].trim());

        java.util.Comparator<java.util.Map.Entry<String, List<Transaction>>> cmp;
        if ("occurredAt".equalsIgnoreCase(prop)) {
            cmp = java.util.Comparator.comparing(
                    e -> e.getValue().stream().map(Transaction::getOccurredAt).max(Instant::compareTo).orElse(Instant.EPOCH)
            );
        } else {
            cmp = java.util.Comparator.comparing(
                    e -> e.getValue().stream().map(Transaction::getOccurredAt).max(Instant::compareTo).orElse(Instant.EPOCH)
            );
        }
        if (desc) cmp = cmp.reversed();

        var sorted = filteredGroups.stream().sorted(cmp).toList();

        // 6) Paginacija nad grupama (transferima)
        int fromIdx = Math.max(page, 0) * Math.max(size, 1);
        if (fromIdx >= sorted.size()) {
            return ResponseEntity.ok(List.of());
        }
        int toIdx = Math.min(fromIdx + Math.max(size, 1), sorted.size());
        var pageSlice = sorted.subList(fromIdx, toIdx);

        // 7) Mapiranje u TransferResponse
        var result = pageSlice.stream().map(e -> {
            var list = e.getValue();

            Transaction tOut = list.stream()
                    .filter(t -> t.getCategory().getType() == CategoryType.EXPENSE)
                    .findFirst().orElse(null);

            Transaction tIn = list.stream()
                    .filter(t -> t.getCategory().getType() == CategoryType.INCOME)
                    .findFirst().orElse(null);

            var outDto = (tOut != null) ? toTransactionResponse(tOut) : null;
            var inDto  = (tIn  != null) ? toTransactionResponse(tIn)  : null;

            return new TransferResponse(e.getKey(), outDto, inDto);
        }).toList();

        return ResponseEntity.ok(result);
    }

    private Instant parseFrom(String v) {
        if (v == null || v.isBlank()) return null;
        if (v.length() == 10) { // "yyyy-MM-dd"
            LocalDate d = LocalDate.parse(v);
            return d.atStartOfDay(ZoneOffset.UTC).toInstant();
        }
        return Instant.parse(v); // pun ISO instant
    }

    private Instant parseTo(String v) {
        if (v == null || v.isBlank()) return null;
        if (v.length() == 10) { // "yyyy-MM-dd"
            LocalDate d = LocalDate.parse(v);
            // kraj dana (exclusive next day start minus 1ns)
            return d.plusDays(1).atStartOfDay(ZoneOffset.UTC).minusNanos(1).toInstant();
        }
        return Instant.parse(v); // pun ISO instant
    }

    // UPDATE transfera (PUT /api/transfers/{transferId})
    @PutMapping("/{transferId}")
    @Transactional
    public ResponseEntity<List<TransactionResponse>> updateTransfer(
            @PathVariable String transferId,
            @Valid @RequestBody group19.WebFinanceApp.controller.dto.request.TransferUpdateRequest in
    ) {
        //  ne menjamo transferId
        if (in.getNewTransferId() != null && !in.getNewTransferId().isBlank()
                && !in.getNewTransferId().equals(transferId)) {
            return ResponseEntity.badRequest().build();
        }

        var list = transactions.findByTransferIdOrderByOccurredAtAsc(transferId);
        if (list == null || list.isEmpty()) return ResponseEntity.notFound().build();

        Transaction tOut = list.stream()
                .filter(t -> t.getCategory().getType() == CategoryType.EXPENSE)
                .findFirst().orElse(null);
        Transaction tIn = list.stream()
                .filter(t -> t.getCategory().getType() == CategoryType.INCOME)
                .findFirst().orElse(null);
        if (tOut == null || tIn == null) return ResponseEntity.status(HttpStatus.CONFLICT).build();

        // vrati stara stanja
        Wallet oldFrom = tOut.getWallet();
        Wallet oldTo   = tIn.getWallet();
        oldFrom.setBalance(oldFrom.getBalance().add(tOut.getAmount()));
        oldTo.setBalance(oldTo.getBalance().subtract(tIn.getAmount()));
        wallets.save(oldFrom);
        wallets.save(oldTo);

        // validacija novih resursa
        if (in.getFromWalletId().equals(in.getToWalletId())) return ResponseEntity.badRequest().build();

        Wallet newFrom = wallets.findById(in.getFromWalletId()).orElse(null);
        Wallet newTo   = wallets.findById(in.getToWalletId()).orElse(null);
        if (newFrom == null || newTo == null) return ResponseEntity.badRequest().build();
        if (newFrom.isArchived() || newTo.isArchived()) return ResponseEntity.status(HttpStatus.CONFLICT).build();

        Category outCat = categories.findById(in.getOutCategoryId()).orElse(null);
        Category inCat  = categories.findById(in.getInCategoryId()).orElse(null);
        if (outCat == null || inCat == null) return ResponseEntity.badRequest().build();
        if (outCat.getType() != CategoryType.EXPENSE || inCat.getType() != CategoryType.INCOME) {
            return ResponseEntity.badRequest().build();
        }

        BigDecimal amountOut = in.getAmount(); // i dalje u FROM valuti
        if (newFrom.getBalance().compareTo(amountOut) < 0) return ResponseEntity.status(HttpStatus.CONFLICT).build();

        // preračunaj amountIn po novim valutama
        BigDecimal amountIn;
        if (newFrom.getCurrency().getId().equals(newTo.getCurrency().getId())) {
            amountIn = amountOut;
        } else {
            BigDecimal fromVsEur = newFrom.getCurrency().getValueVsEur();
            BigDecimal toVsEur   = newTo.getCurrency().getValueVsEur();
            amountIn = amountOut.multiply(fromVsEur).divide(toVsEur, 2, RoundingMode.HALF_UP);
        }

        Instant when = (in.getOccurredAt() != null) ? in.getOccurredAt() : Instant.now();

        // ažuriraj transakcije
        tOut.setWallet(newFrom);
        tOut.setCategory(outCat);
        tOut.setAmount(amountOut);
        tOut.setDescription(in.getDescription());
        tOut.setOccurredAt(when);
        tOut.setTransferId(transferId);

        tIn.setWallet(newTo);
        tIn.setCategory(inCat);
        tIn.setAmount(amountIn);
        tIn.setDescription(in.getDescription());
        tIn.setOccurredAt(when);
        tIn.setTransferId(transferId);

        // nova stanja
        newFrom.setBalance(newFrom.getBalance().subtract(amountOut));
        newTo.setBalance(newTo.getBalance().add(amountIn));
        wallets.save(newFrom);
        wallets.save(newTo);

        tOut = transactions.save(tOut);
        tIn  = transactions.save(tIn);

        var body = List.of(toTransactionResponse(tOut), toTransactionResponse(tIn));
        return ResponseEntity.ok(body);
    }

    // DELETE transfera (DELETE /api/transfers/{transferId})
    @DeleteMapping("/{transferId}")
    @Transactional
    public ResponseEntity<Void> deleteTransfer(@PathVariable String transferId) {
        var list = transactions.findByTransferIdOrderByOccurredAtAsc(transferId);
        if (list == null || list.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Transaction tOut = list.stream()
                .filter(t -> t.getCategory().getType() == CategoryType.EXPENSE)
                .findFirst().orElse(null);
        Transaction tIn = list.stream()
                .filter(t -> t.getCategory().getType() == CategoryType.INCOME)
                .findFirst().orElse(null);

        if (tOut != null) {
            Wallet from = tOut.getWallet();
            from.setBalance(from.getBalance().add(tOut.getAmount()));
            wallets.save(from);
        }
        if (tIn != null) {
            Wallet to = tIn.getWallet();
            to.setBalance(to.getBalance().subtract(tIn.getAmount()));
            wallets.save(to);
        }

        transactions.deleteAll(list);
        return ResponseEntity.noContent().build();
    }
}