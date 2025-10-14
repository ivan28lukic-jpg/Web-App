package group19.WebFinanceApp.controller;

import group19.WebFinanceApp.controller.dto.request.TransactionCreateRequest;
import group19.WebFinanceApp.controller.dto.request.TransactionUpdateRequest;
import group19.WebFinanceApp.controller.dto.request.TransferCreateRequest;
import group19.WebFinanceApp.controller.dto.response.PeriodStatsResponse;
import group19.WebFinanceApp.controller.dto.response.TransactionResponse;
import group19.WebFinanceApp.model.Category;
import group19.WebFinanceApp.model.CategoryType;
import group19.WebFinanceApp.model.Transaction;
import group19.WebFinanceApp.model.Wallet;
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
import org.springframework.web.server.ResponseStatusException;
import group19.WebFinanceApp.service.TransferService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final TransferService transferService;

    public TransactionController(TransactionRepository transactionRepository,
                                 WalletRepository walletRepository,
                                 CategoryRepository categoryRepository,
                                 TransferService transferService) {
        this.transactionRepository = transactionRepository;
        this.walletRepository = walletRepository;
        this.categoryRepository = categoryRepository;
        this.transferService = transferService;
    }

    // LIST
    @GetMapping
    public List<TransactionResponse> list(
            @RequestParam(value = "walletId", required = false) Long walletId,
            @RequestParam(value = "ownerId", required = false) Long ownerId) {

        List<Transaction> list;
        if (walletId != null) {
            list = transactionRepository.findByWalletId(walletId);
        } else if (ownerId != null) {
            list = transactionRepository.findByWalletOwnerId(ownerId);
        } else {
            list = transactionRepository.findAll();
        }
        return list.stream().map(this::toResponse).toList();
    }

    // GET by id
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> get(@PathVariable Long id) {
        return transactionRepository.findById(id)
                .map(t -> ResponseEntity.ok(toResponse(t)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // CREATE (pojedinačna transakcija)
    @PostMapping
    @Transactional
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionCreateRequest req) {
        Wallet wallet = walletRepository.findById(req.getWalletId())
                .orElseThrow(() -> new IllegalArgumentException("Wallet not found: " + req.getWalletId()));

        Category category = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.getCategoryId()));

        if (req.getOccurredAt() == null) {
            req.setOccurredAt(Instant.now());
        }

        // 1) ažuriraj balans
        BigDecimal delta = signedAmount(category.getType(), req.getAmount());
        wallet.setBalance(wallet.getBalance().add(delta));
        walletRepository.save(wallet);

        // 2) snimi transakciju
        Transaction t = new Transaction();
        t.setWallet(wallet);
        t.setCategory(category);
        t.setAmount(req.getAmount());
        t.setDescription(req.getDescription());
        t.setOccurredAt(req.getOccurredAt());
        t.setTransferId(req.getTransferId()); // može biti null
        Transaction saved = transactionRepository.save(t);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    // UPDATE
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<TransactionResponse> update(@PathVariable Long id,
                                                      @Valid @RequestBody TransactionUpdateRequest req) {
        Optional<Transaction> opt = transactionRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Transaction t = opt.get();
        Wallet wallet = t.getWallet();

        // skini stari uticaj na balans
        BigDecimal oldDelta = signedAmount(t.getCategory().getType(), t.getAmount());
        wallet.setBalance(wallet.getBalance().subtract(oldDelta));

        // nova kategorija / iznos
        Category newCategory = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + req.getCategoryId()));

        BigDecimal newDelta = signedAmount(newCategory.getType(), req.getAmount());
        wallet.setBalance(wallet.getBalance().add(newDelta));
        walletRepository.save(wallet);

        t.setCategory(newCategory);
        t.setAmount(req.getAmount());
        t.setDescription(req.getDescription());
        if (req.getOccurredAt() != null) {
            t.setOccurredAt(req.getOccurredAt());
        }
        if (req.getTransferId() != null) {
            t.setTransferId(req.getTransferId());
        }
        Transaction saved = transactionRepository.save(t);

        return ResponseEntity.ok(toResponse(saved));
    }

    // DELETE
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<Transaction> opt = transactionRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Transaction t = opt.get();

        // vrati uticaj na balans
        Wallet wallet = t.getWallet();
        BigDecimal delta = signedAmount(t.getCategory().getType(), t.getAmount());
        wallet.setBalance(wallet.getBalance().subtract(delta));
        walletRepository.save(wallet);

        transactionRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // TRANSFER: jedan poziv -> dve transakcije sa istim transferId (sa automatskom FX konverzijom)
    @PostMapping("/transfer")
    @Transactional
    public ResponseEntity<List<TransactionResponse>> createTransfer(
            @Valid @RequestBody TransferCreateRequest req) {

        if (req.getFromWalletId().equals(req.getToWalletId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromWalletId and toWalletId must differ");
        }

        var from = walletRepository.findById(req.getFromWalletId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "From wallet not found"));
        var to = walletRepository.findById(req.getToWalletId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "To wallet not found"));

        // (opciono) – dozvoli transfer samo unutar istog vlasnika
        if (!from.getOwner().getId().equals(to.getOwner().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer allowed only between wallets of same owner");
        }

        // Kategorije obavezne i pravilnog tipa (EXPENSE za izlaz, INCOME za ulaz)
        var outCat = categoryRepository.findById(req.getOutCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense category not found"));
        var inCat = categoryRepository.findById(req.getInCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Income category not found"));

        if (outCat.getType() != CategoryType.EXPENSE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "outCategoryId must be EXPENSE");
        }
        if (inCat.getType() != CategoryType.INCOME) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "inCategoryId must be INCOME");
        }

        if (from.getBalance().compareTo(req.getAmount()) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient funds on source wallet");
        }

        Instant when = (req.getOccurredAt() != null) ? req.getOccurredAt() : Instant.now();
        String transferId = (req.getTransferId() != null && !req.getTransferId().isBlank())
                ? req.getTransferId()
                : java.util.UUID.randomUUID().toString();

        // --- IZNOSI ---
        // amountFrom je u valuti izvornog novčanika
        java.math.BigDecimal amountFrom = req.getAmount();

        // Ako su valute različite, preračunaj amountTo po kursu EUR->CODE (valueVsEur)
        java.math.BigDecimal amountTo;
        var fromCur = from.getCurrency();
        var toCur = to.getCurrency();

        if (fromCur.getId().equals(toCur.getId())) {
            amountTo = amountFrom; // ista valuta
        } else {
            // 1 EUR = rate(EUR->CODE)
            // amountTo = amountFrom / rateFrom * rateTo
            java.math.BigDecimal rateFrom = fromCur.getValueVsEur(); // npr. RSD: 117.000000
            java.math.BigDecimal rateTo   = toCur.getValueVsEur();   // npr. USD: 1.100000

            if (rateFrom == null || rateTo == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Currency rates not available");
            }

            java.math.BigDecimal eurAmount = amountFrom.divide(rateFrom, 8, java.math.RoundingMode.HALF_UP);
            amountTo = eurAmount.multiply(rateTo).setScale(2, java.math.RoundingMode.HALF_UP);
        }

        // 1) izlaz (EXPENSE) sa izvornog novčanika – amountFrom
        var tOut = new Transaction();
        tOut.setWallet(from);
        tOut.setCategory(outCat);
        tOut.setAmount(amountFrom);
        tOut.setDescription(req.getDescription());
        tOut.setOccurredAt(when);
        tOut.setTransferId(transferId);
        transactionRepository.save(tOut);

        // 2) ulaz (INCOME) na odredišni novčanik – amountTo (posle konverzije)
        var tIn = new Transaction();
        tIn.setWallet(to);
        tIn.setCategory(inCat);
        tIn.setAmount(amountTo);
        tIn.setDescription(req.getDescription());
        tIn.setOccurredAt(when);
        tIn.setTransferId(transferId);
        transactionRepository.save(tIn);

        // ažuriraj stanja
        from.setBalance(from.getBalance().subtract(amountFrom));
        to.setBalance(to.getBalance().add(amountTo));
        walletRepository.save(from);
        walletRepository.save(to);

        var body = java.util.List.of(toResponse(tOut), toResponse(tIn));
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    // Napredna pretraga
    @GetMapping("/search")
    public ResponseEntity<Page<TransactionResponse>> advancedSearch(
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) CategoryType categoryType,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to")   String toStr,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(required = false, name = "q") String q,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "occurredAt,desc") String sort
    ) {
        Pageable pageable = makePageable(page, size, sort);
        Instant from = parseFrom(fromStr);
        Instant to   = parseTo(toStr);

        Page<Transaction> result = transactionRepository.search(
                walletId, ownerId, categoryId, categoryType,
                from, to, minAmount, maxAmount, q, pageable
        );

        Page<TransactionResponse> mapped = result.map(this::toResponse);
        return ResponseEntity.ok(mapped);
    }

    /* =====================  STATISTIKA  ===================== */

    // ---------- TOP KATEGORIJE (troškovi) ----------
    @GetMapping("/stats/top-categories")
    public ResponseEntity<List<TopCategoryResponse>> topExpenseCategories(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to")   String toStr,
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (limit <= 0) limit = 10;
        var from = parseFrom(fromStr);
        var to   = parseTo(toStr);

        // ukupni trošak u periodu (za % učešća)
        BigDecimal total = Optional
                .ofNullable(transactionRepository.totalExpense(ownerId, walletId, from, to))
                .orElse(BigDecimal.ZERO);

        Pageable topN = PageRequest.of(0, limit, Sort.unsorted());
        List<TransactionRepository.TopCategoryRow> rows =
                transactionRepository.topExpenseCategories(ownerId, walletId, from, to, topN);

        List<TopCategoryResponse> body = rows.stream()
                .map(r -> {
                    BigDecimal catTotal = Optional
                            .ofNullable(r.getTotalAmount())
                            .orElse(BigDecimal.ZERO);

                    BigDecimal share = BigDecimal.ZERO;
                    if (total.compareTo(BigDecimal.ZERO) > 0) {
                        share = catTotal.multiply(BigDecimal.valueOf(100))
                                .divide(total, 2, java.math.RoundingMode.HALF_UP);
                    }
                    return new TopCategoryResponse(
                            r.getCategoryId(),
                            r.getCategoryName(),
                            catTotal,
                            share
                    );
                })
                .toList();

        return ResponseEntity.ok(body);
    }

    // ---------- DAILY (categoryType varijanta) ----------
    @GetMapping("/stats/daily")
    public ResponseEntity<List<PeriodStatsResponse>> statsDaily(
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) CategoryType categoryType,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to")   String toStr
    ) {
        var from = parseFrom(fromStr);
        var to   = parseTo(toStr);
        var rows = transactionRepository.statsDaily(walletId, ownerId, categoryType, from, to);
        List<PeriodStatsResponse> body = rows.stream().map(this::mapPeriodRow).toList();
        return ResponseEntity.ok(body);
    }

    // ---------- WEEKLY ----------
    @GetMapping(value = "/stats/weekly", params = {"categoryType", "!categoryId"})
    public ResponseEntity<List<PeriodStatsResponse>> statsWeeklyByType(
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam CategoryType categoryType,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to")   String toStr
    ) {
        var from = parseFrom(fromStr);
        var to   = parseTo(toStr);
        var rows = transactionRepository.statsWeekly(walletId, ownerId, categoryType, from, to);
        List<PeriodStatsResponse> body = rows.stream().map(this::mapPeriodRow).toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/stats/weekly", params = {"categoryId", "!categoryType"})
    public ResponseEntity<List<PeriodStatsResponse>> statsWeeklyByCategory(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long walletId,
            @RequestParam Long categoryId,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to")   String toStr
    ) {
        var from = parseFrom(fromStr);
        var to   = parseTo(toStr);
        var rows = transactionRepository.statsWeekly(ownerId, walletId, categoryId, from, to);
        List<PeriodStatsResponse> body = rows.stream().map(this::mapPeriodRow).toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/stats/weekly", params = {"!categoryId", "!categoryType"})
    public ResponseEntity<List<PeriodStatsResponse>> statsWeeklyNoFilter(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to")   String toStr
    ) {
        var from = parseFrom(fromStr);
        var to   = parseTo(toStr);
        var rows = transactionRepository.statsWeekly(walletId, ownerId, (CategoryType) null, from, to);
        List<PeriodStatsResponse> body = rows.stream().map(this::mapPeriodRow).toList();
        return ResponseEntity.ok(body);
    }

    // ---------- MONTHLY ----------
    @GetMapping(value = "/stats/monthly", params = {"categoryType", "!categoryId"})
    public ResponseEntity<List<PeriodStatsResponse>> statsMonthlyByType(
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam CategoryType categoryType,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to")   String toStr
    ) {
        var from = parseFrom(fromStr);
        var to   = parseTo(toStr);
        var rows = transactionRepository.statsMonthly(walletId, ownerId, categoryType, from, to);
        List<PeriodStatsResponse> body = rows.stream().map(this::mapPeriodRow).toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/stats/monthly", params = {"categoryId", "!categoryType"})
    public ResponseEntity<List<PeriodStatsResponse>> statsMonthlyByCategory(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long walletId,
            @RequestParam Long categoryId,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to")   String toStr
    ) {
        var from = parseFrom(fromStr);
        var to   = parseTo(toStr);
        var rows = transactionRepository.statsMonthly(ownerId, walletId, categoryId, from, to);
        List<PeriodStatsResponse> body = rows.stream().map(this::mapPeriodRow).toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/stats/monthly", params = {"!categoryType", "!categoryId"})
    public ResponseEntity<List<PeriodStatsResponse>> statsMonthlyNoFilter(
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to")   String toStr
    ) {
        var from = parseFrom(fromStr);
        var to   = parseTo(toStr);
        var rows = transactionRepository.statsMonthly(walletId, ownerId, (CategoryType) null, from, to);
        List<PeriodStatsResponse> body = rows.stream().map(this::mapPeriodRow).toList();
        return ResponseEntity.ok(body);
    }

    // ---------- QUARTERLY ----------
    @GetMapping("/stats/quarterly")
    public ResponseEntity<List<PeriodStatsResponse>> statsQuarterly(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to")   String toStr
    ) {
        var from = parseFrom(fromStr);
        var to   = parseTo(toStr);
        var rows = transactionRepository.statsQuarterly(ownerId, walletId, categoryId, from, to);
        List<PeriodStatsResponse> body = rows.stream().map(this::mapPeriodRow).toList();
        return ResponseEntity.ok(body);
    }

    // ---------- YEARLY ----------
    @GetMapping(value = "/stats/yearly", params = {"categoryType", "!categoryId"})
    public ResponseEntity<List<PeriodStatsResponse>> statsYearlyByType(
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false) Long ownerId,
            @RequestParam CategoryType categoryType,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to")   String toStr
    ) {
        var from = parseFrom(fromStr);
        var to   = parseTo(toStr);
        var rows = transactionRepository.statsYearly(walletId, ownerId, categoryType, from, to);
        List<PeriodStatsResponse> body = rows.stream().map(this::mapPeriodRow).toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/stats/yearly", params = {"categoryId", "!categoryType"})
    public ResponseEntity<List<PeriodStatsResponse>> statsYearlyByCategory(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long walletId,
            @RequestParam Long categoryId,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to")   String toStr
    ) {
        var from = parseFrom(fromStr);
        var to   = parseTo(toStr);
        var rows = transactionRepository.statsYearly(ownerId, walletId, categoryId, from, to);
        List<PeriodStatsResponse> body = rows.stream().map(this::mapPeriodRow).toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping(value = "/stats/yearly", params = {"!categoryId", "!categoryType"})
    public ResponseEntity<List<PeriodStatsResponse>> statsYearlyNoFilter(
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long walletId,
            @RequestParam(required = false, name = "from") String fromStr,
            @RequestParam(required = false, name = "to")   String toStr
    ) {
        var from = parseFrom(fromStr);
        var to   = parseTo(toStr);
        var rows = transactionRepository.statsYearly(walletId, ownerId, (CategoryType) null, from, to);
        List<PeriodStatsResponse> body = rows.stream().map(this::mapPeriodRow).toList();
        return ResponseEntity.ok(body);
    }

    /* --- helperi --- */

    private PeriodStatsResponse mapPeriodRow(TransactionRepository.PeriodAgg r) {
        BigDecimal income = nz(r.getIncome());
        BigDecimal expense = nz(r.getExpense());
        BigDecimal net = income.subtract(expense);
        return new PeriodStatsResponse(r.getPeriod(), income, expense, net);
    }

    private PeriodStatsResponse mapPeriodRow(TransactionRepository.PeriodStatRow r) {
        return new PeriodStatsResponse(
                r.getPeriod(),
                nz(r.getIncome()),
                nz(r.getExpense()),
                nz(r.getNet())
        );
    }

    private TransactionResponse toResponse(Transaction tx) {
        TransactionResponse r = new TransactionResponse();
        r.setId(tx.getId());
        r.setWalletId(tx.getWallet().getId());
        r.setCategoryId(tx.getCategory().getId());
        r.setCategoryName(tx.getCategory().getName());
        r.setCategoryType(tx.getCategory().getType());
        r.setAmount(tx.getAmount());
        r.setDescription(tx.getDescription());
        r.setOccurredAt(tx.getOccurredAt());
        r.setTransferId(tx.getTransferId());

        // Ako su dodate veze na recurring entitete, popuni ID-jeve (opciono)
        if (tx.getRecurringTemplate() != null) {
            r.setRecurringTemplateId(tx.getRecurringTemplate().getId());
        }
        if (tx.getRecurringInstance() != null) {
            r.setRecurringInstanceId(tx.getRecurringInstance().getId());
        }

        return r;
    }

    private BigDecimal signedAmount(CategoryType type, BigDecimal amount) {
        return (type == CategoryType.INCOME) ? amount : amount.negate();
    }

    private Pageable makePageable(int page, int size, String sortParam) {
        Sort sort = Sort.by("occurredAt").descending();
        if (sortParam != null && !sortParam.isBlank()) {
            String[] parts = sortParam.split(",", 2);
            String field = parts[0].trim();
            String dir = (parts.length > 1 ? parts[1].trim() : "desc");
            Sort.Direction direction = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

            if ("occurredAt".equalsIgnoreCase(field) || "amount".equalsIgnoreCase(field) || "id".equalsIgnoreCase(field)) {
                sort = Sort.by(direction, field);
            }
        }
        return PageRequest.of(page, size, sort);
    }

    private Instant parseFrom(String v) {
        if (v == null || v.isBlank()) return null;
        if (v.length() == 10) { // yyyy-MM-dd
            return LocalDate.parse(v).atStartOfDay(ZoneOffset.UTC).toInstant();
        }
        return Instant.parse(v);
    }

    private Instant parseTo(String v) {
        if (v == null || v.isBlank()) return null;
        if (v.length() == 10) { // yyyy-MM-dd
            return LocalDate.parse(v).plusDays(1).atStartOfDay(ZoneOffset.UTC).minusNanos(1).toInstant();
        }
        return Instant.parse(v);
    }

    private BigDecimal nz(BigDecimal x) {
        return x != null ? x : BigDecimal.ZERO;
    }

    /* ---------- DTO za odgovor (lokalni da ne dodajemo fajl) ---------- */
    public static class TopCategoryResponse {
        private Long categoryId;
        private String categoryName;
        private BigDecimal totalAmount;
        private BigDecimal sharePercent;

        public TopCategoryResponse() {}
        public TopCategoryResponse(Long categoryId, String categoryName,
                                   BigDecimal totalAmount, BigDecimal sharePercent) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
            this.totalAmount = totalAmount;
            this.sharePercent = sharePercent;
        }
        public Long getCategoryId() { return categoryId; }
        public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public BigDecimal getSharePercent() { return sharePercent; }
        public void setSharePercent(BigDecimal sharePercent) { this.sharePercent = sharePercent; }
    }
}