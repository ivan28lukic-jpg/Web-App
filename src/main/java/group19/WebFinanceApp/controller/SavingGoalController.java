package group19.WebFinanceApp.controller;

import group19.WebFinanceApp.controller.dto.request.SavingGoalContributeRequest;
import group19.WebFinanceApp.controller.dto.request.SavingGoalCreateRequest;
import group19.WebFinanceApp.controller.dto.request.SavingGoalUpdateRequest;
import group19.WebFinanceApp.controller.dto.request.SavingGoalWithdrawRequest;
import group19.WebFinanceApp.controller.dto.response.SavingGoalResponse;
import group19.WebFinanceApp.model.SavingGoal;
import group19.WebFinanceApp.model.User;
import group19.WebFinanceApp.model.Wallet;
import group19.WebFinanceApp.repository.SavingGoalRepository;
import group19.WebFinanceApp.repository.UserRepository;
import group19.WebFinanceApp.repository.WalletRepository;
import group19.WebFinanceApp.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import group19.WebFinanceApp.controller.dto.response.SavingGoalProgressPoint;
import group19.WebFinanceApp.controller.dto.response.SavingGoalProgressResponse;
import group19.WebFinanceApp.model.CategoryType;
import group19.WebFinanceApp.model.Transaction;
import group19.WebFinanceApp.repository.TransactionRepository;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.List;
import java.time.*;   // <-- za LocalDate, ZoneOffset
import java.util.Comparator;

@RestController
@RequestMapping("/api/saving-goals")
public class SavingGoalController {

    private final SavingGoalRepository goals;
    private final UserRepository users;
    private final WalletRepository wallets;
    private final TransferService transferService;
    private final TransactionRepository transactions;

    public SavingGoalController(SavingGoalRepository goals,
                                UserRepository users,
                                WalletRepository wallets,
                                TransferService transferService,
                                TransactionRepository transactions) {
        this.goals = goals;
        this.users = users;
        this.wallets = wallets;
        this.transferService = transferService;
        this.transactions = transactions;
    }

    // LIST by owner (includeArchived=false podrazumevano)
    @GetMapping("/owner/{ownerId}")
    public List<SavingGoalResponse> byOwner(@PathVariable Long ownerId,
                                            @RequestParam(defaultValue = "false") boolean includeArchived) {
        return goals.findByOwnerId(ownerId).stream()
                .filter(g -> includeArchived || !g.isArchived())
                .map(this::toResponse)
                .toList();
    }

    // ADMIN list
    @GetMapping
    public List<SavingGoalResponse> all(@RequestParam(defaultValue = "false") boolean includeArchived) {
        return goals.findAll().stream()
                .filter(g -> includeArchived || !g.isArchived())
                .map(this::toResponse)
                .toList();
    }

    // GET by id
    @GetMapping("/{id}")
    public ResponseEntity<SavingGoalResponse> byId(@PathVariable Long id) {
        return goals.findById(id)
                .map(g -> ResponseEntity.ok(toResponse(g)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // CREATE
    @PostMapping
    public ResponseEntity<SavingGoalResponse> create(@Valid @RequestBody SavingGoalCreateRequest in) {
        User owner = users.findById(in.getOwnerId()).orElse(null);
        if (owner == null) return ResponseEntity.notFound().build();

        Wallet w = wallets.findById(in.getWalletId()).orElse(null);
        if (w == null) return ResponseEntity.notFound().build();

        // Wallet mora pripadati istom vlasniku
        if (!w.getOwner().getId().equals(owner.getId())) {
            return ResponseEntity.badRequest().build();
        }

        // Dozvoljeno samo na štedni wallet
        if (!w.isSavings()) {
            return ResponseEntity.badRequest().build();
        }

        // Jedinstveno ime cilja štednje po vlasniku
        if (goals.existsByOwnerIdAndNameIgnoreCase(owner.getId(), in.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        SavingGoal g = new SavingGoal();
        g.setOwner(owner);
        g.setWallet(w);
        g.setName(in.getName());
        g.setTargetAmount(in.getTargetAmount());
        g.setCurrentAmount(BigDecimal.ZERO);
        g.setDueDate(in.getDueDate());
        g.setArchived(false);

        g = goals.save(g);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(g));
    }

    // UPDATE (name/targetAmount/dueDate/archived)
    @PutMapping("/{id}")
    public ResponseEntity<SavingGoalResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody SavingGoalUpdateRequest in) {
        return goals.findById(id)
                .map(g -> {
                    Long ownerId = g.getOwner().getId();
                    if (goals.existsByOwnerIdAndNameIgnoreCaseAndIdNot(ownerId, in.getName(), id)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).<SavingGoalResponse>build();
                    }
                    g.setName(in.getName());
                    g.setTargetAmount(in.getTargetAmount());
                    g.setDueDate(in.getDueDate());
                    g.setArchived(in.isArchived());
                    goals.save(g);
                    return ResponseEntity.ok(toResponse(g));
                })
                .orElseGet(() -> ResponseEntity.notFound().<SavingGoalResponse>build());
    }

    // CONTRIBUTE
    @PostMapping("/{id}/contribute")
    public ResponseEntity<SavingGoalResponse> contribute(
            @PathVariable Long id,
            @RequestBody(required = false) @Valid SavingGoalContributeRequest in,
            @RequestParam(required = false) java.math.BigDecimal amount,
            @RequestParam(required = false, name = "fromWalletId") Long fromWalletId,
            @RequestParam(required = false, name = "sourceWalletId") Long sourceWalletId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) java.time.Instant occurredAt,
            @RequestParam(required = false) Long outCategoryId,
            @RequestParam(required = false) Long inCategoryId
    ) {
        var opt = goals.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        SavingGoal g = opt.get();
        if (g.isArchived()) return ResponseEntity.status(HttpStatus.CONFLICT).build();

        // merge body + query (bez novih klasa)
        if (in == null) in = new SavingGoalContributeRequest();
        if (in.getAmount() == null && amount != null) in.setAmount(amount);
        if (in.getFromWalletId() == null) {
            if (fromWalletId != null) in.setFromWalletId(fromWalletId);
            else if (sourceWalletId != null) in.setFromWalletId(sourceWalletId); // alias
        }
        if (in.getDescription() == null && description != null && !description.isBlank()) in.setDescription(description);
        if (in.getOccurredAt() == null) in.setOccurredAt(occurredAt);
        if (in.getOutCategoryId() == null) in.setOutCategoryId(outCategoryId);
        if (in.getInCategoryId() == null) in.setInCategoryId(inCategoryId);

        var result = transferService.transfer(
                in.getFromWalletId(),
                g.getWallet().getId(),
                in.getAmount(),
                in.getDescription(),
                in.getOccurredAt(),
                in.getOutCategoryId(),
                in.getInCategoryId()
        );

        java.math.BigDecimal credited = result.inTx().getAmount();
        g.setCurrentAmount(g.getCurrentAmount().add(credited));
        goals.save(g);

        return ResponseEntity.ok(toResponse(g));
    }

    // WITHDRAW
    @PostMapping("/{id}/withdraw")
    public ResponseEntity<SavingGoalResponse> withdraw(
            @PathVariable Long id,
            @RequestBody(required = false) @Valid SavingGoalWithdrawRequest in,
            @RequestParam(required = false) java.math.BigDecimal amount,
            @RequestParam(required = false, name = "toWalletId") Long toWalletId,
            @RequestParam(required = false, name = "destinationWalletId") Long destinationWalletId,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) java.time.Instant occurredAt,
            @RequestParam(required = false) Long outCategoryId,
            @RequestParam(required = false) Long inCategoryId
    ) {
        var opt = goals.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        SavingGoal g = opt.get();
        if (g.isArchived()) return ResponseEntity.status(HttpStatus.CONFLICT).build();

        // merge body + query
        if (in == null) in = new SavingGoalWithdrawRequest();
        if (in.getAmount() == null && amount != null) in.setAmount(amount);
        if (in.getToWalletId() == null) {
            if (toWalletId != null) in.setToWalletId(toWalletId);
            else if (destinationWalletId != null) in.setToWalletId(destinationWalletId); // alias
        }
        if (in.getDescription() == null && description != null && !description.isBlank()) in.setDescription(description);
        if (in.getOccurredAt() == null) in.setOccurredAt(occurredAt);
        if (in.getOutCategoryId() == null) in.setOutCategoryId(outCategoryId);
        if (in.getInCategoryId() == null) in.setInCategoryId(inCategoryId);

        // ne dozvoli minus na goal-u
        if (g.getCurrentAmount().compareTo(in.getAmount()) < 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        var result = transferService.transfer(
                g.getWallet().getId(),
                in.getToWalletId(),
                in.getAmount(),
                in.getDescription(),
                in.getOccurredAt(),
                in.getOutCategoryId(),
                in.getInCategoryId()
        );

        g.setCurrentAmount(g.getCurrentAmount().subtract(in.getAmount()));
        goals.save(g);

        return ResponseEntity.ok(toResponse(g));
    }

    // DELETE (hard delete)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return goals.findById(id)
                .map(g -> { goals.delete(g); return ResponseEntity.noContent().<Void>build(); })
                .orElseGet(() -> ResponseEntity.notFound().<Void>build());
    }

    // PROGRESS (sa grafikom) – vremenska serija IN transakcija na goal.wallet
    @GetMapping("/{id}/progress")
    public ResponseEntity<SavingGoalProgressResponse> progress(
            @PathVariable Long id,
            // možeš ili ISO datume (Instant)...
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            // ...ili čiste LocalDate datume (YYYY-MM-DD) – prioritet u odnosu na Instant
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        var opt = goals.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        SavingGoal g = opt.get();

        // vremenski opseg
        Instant effFrom = (fromDate != null)
                ? fromDate.atStartOfDay(ZoneOffset.UTC).toInstant()
                : (from != null ? from : g.getCreatedAt()); // default: od kreiranja cilja

        Instant effTo = (toDate != null)
                ? toDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusNanos(1)
                : (to != null ? to : Instant.now());

        // Sve transakcije za goal.wallet u opsegu
        var txs = transactions.findByWalletIdAndOccurredAtBetween(g.getWallet().getId(), effFrom, effTo);
        // Uzimamo samo PRILIVE (INCOME) na taj wallet kao "uplate na cilj"
        var incomeTxs = txs.stream()
                .filter(t -> t.getCategory() != null && t.getCategory().getType() == CategoryType.INCOME)
                .sorted(Comparator.comparing(Transaction::getOccurredAt))
                .toList();

        // Timeseries – kumulativno
        java.math.BigDecimal cumulative = java.math.BigDecimal.ZERO;
        var points = new java.util.ArrayList<SavingGoalProgressPoint>();
        for (var t : incomeTxs) {
            java.math.BigDecimal amt = t.getAmount();
            cumulative = cumulative.add(amt);
            points.add(new SavingGoalProgressPoint(t.getOccurredAt(), amt, cumulative));
        }

        // Sažetak
        var target = g.getTargetAmount();
        var current = g.getCurrentAmount();
        if (current == null) current = java.math.BigDecimal.ZERO;

        var remaining = target.subtract(current);
        if (remaining.signum() < 0) remaining = java.math.BigDecimal.ZERO;

        java.math.BigDecimal percent = java.math.BigDecimal.ZERO;
        if (target.signum() > 0) {
            percent = current.multiply(java.math.BigDecimal.valueOf(100))
                    .divide(target, 2, java.math.RoundingMode.HALF_UP);
        }

        Long daysLeft = null;
        java.math.BigDecimal neededPerDay = null;
        if (g.getDueDate() != null) {
            long d = java.time.Duration.between(
                    java.time.LocalDate.now(java.time.ZoneOffset.UTC).atStartOfDay(java.time.ZoneOffset.UTC),
                    g.getDueDate().atStartOfDay(java.time.ZoneOffset.UTC)
            ).toDays();
            daysLeft = d;
            if (d > 0) {
                neededPerDay = remaining.divide(java.math.BigDecimal.valueOf(d), 2, java.math.RoundingMode.HALF_UP);
            }
        }

        var resp = new SavingGoalProgressResponse(
                g.getId(),
                g.getOwner().getId(),
                g.getWallet().getId(),
                g.getWallet().getCurrency().getCode(),
                g.getName(),
                target,
                current,
                remaining,
                percent,
                g.getDueDate(),
                daysLeft,
                neededPerDay,
                effFrom,
                effTo,
                points
        );

        return ResponseEntity.ok(resp);
    }

    // mapper
    private SavingGoalResponse toResponse(SavingGoal g) {
        return new SavingGoalResponse(
                g.getId(),
                g.getOwner().getId(),
                g.getWallet().getId(),
                g.getWallet().getCurrency().getCode(),
                g.getName(),
                g.getTargetAmount(),
                g.getCurrentAmount(),
                g.getDueDate(),
                g.isArchived(),
                g.getCreatedAt(),
                g.getUpdatedAt()
        );
    }
}