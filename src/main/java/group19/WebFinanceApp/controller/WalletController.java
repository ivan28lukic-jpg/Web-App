package group19.WebFinanceApp.controller;

import group19.WebFinanceApp.controller.dto.request.WalletCreateRequest;
import group19.WebFinanceApp.controller.dto.request.WalletUpdateRequest;
import group19.WebFinanceApp.controller.dto.response.WalletResponse;
import group19.WebFinanceApp.model.Currency;
import group19.WebFinanceApp.model.Wallet;
import group19.WebFinanceApp.repository.CurrencyRepository;
import group19.WebFinanceApp.repository.UserRepository;
import group19.WebFinanceApp.repository.WalletRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/wallets")
@CrossOrigin(
        origins = "http://localhost:5173",
        methods = {
                RequestMethod.GET,
                RequestMethod.POST,
                RequestMethod.PUT,
                RequestMethod.DELETE,
                RequestMethod.PATCH,
                RequestMethod.OPTIONS
        },
        allowCredentials = "true"
)
public class WalletController {

    private final WalletRepository wallets;
    private final UserRepository users;
    private final CurrencyRepository currencies;

    public WalletController(WalletRepository wallets,
                            UserRepository users,
                            CurrencyRepository currencies) {
        this.wallets = wallets;
        this.users = users;
        this.currencies = currencies;
    }

    // LIST ?ownerId=&includeArchived=
    @GetMapping
    public List<WalletResponse> list(@RequestParam(required = false) Long ownerId,
                                     @RequestParam(defaultValue = "false") boolean includeArchived) {

        List<Wallet> list = (ownerId != null)
                ? wallets.findByOwnerId(ownerId)
                : wallets.findAll();

        return list.stream()
                .filter(w -> includeArchived || !w.isArchived())
                .map(this::toResponse)
                .toList();
    }

    // GET by id
    @GetMapping("/{id}")
    public ResponseEntity<WalletResponse> get(@PathVariable Long id) {
        Optional<Wallet> opt = wallets.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toResponse(opt.get()));
    }

    // CREATE
    @PostMapping
    @Transactional
    public ResponseEntity<WalletResponse> create(@Valid @RequestBody WalletCreateRequest in) {
        var owner = users.findById(in.getOwnerId()).orElse(null);
        if (owner == null) return ResponseEntity.badRequest().build();

        var code = in.getCurrencyCode() == null ? null : in.getCurrencyCode().trim();
        if (code == null || code.length() != 3) return ResponseEntity.badRequest().build();

        Currency cur = currencies.findByCodeIgnoreCase(code).orElse(null);
        if (cur == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

        // jedinstveno ime po vlasniku
        if (wallets.existsByOwnerIdAndNameIgnoreCase(owner.getId(), in.getName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Wallet w = new Wallet();
        w.setOwner(owner);
        w.setCurrency(cur);
        w.setName(in.getName());
        w.setSavings(in.isSavings());
        w.setArchived(false);
        w.setBalance(in.getInitialBalance());

        w = wallets.save(w);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(w));
    }

    // UPDATE: menja naziv i archived (currency/balance se ovde ne diraju)
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<WalletResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody WalletUpdateRequest in) {
        Optional<Wallet> opt = wallets.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Wallet w = opt.get();
        Long ownerId = w.getOwner().getId();
        if (wallets.existsByOwnerIdAndNameIgnoreCaseAndIdNot(ownerId, in.getName(), id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        w.setName(in.getName());
        w.setArchived(in.isArchived());
        wallets.save(w);

        return ResponseEntity.ok(toResponse(w));
    }

    // DELETE (hard delete)
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<Wallet> opt = wallets.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        wallets.delete(opt.get());
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/wallets/{id}/archive?archived=true|false
    @PatchMapping("/{id}/archive")
    @Transactional
    public ResponseEntity<Void> setArchived(@PathVariable Long id,
                                            @RequestParam boolean archived) {
        Optional<Wallet> opt = wallets.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Wallet w = opt.get();
        if (w.isArchived() != archived) {
            w.setArchived(archived);
            wallets.save(w);
        }
        return ResponseEntity.noContent().build();
    }

    // mapper
    private WalletResponse toResponse(Wallet w) {
        return new WalletResponse(
                w.getId(),
                w.getOwner().getId(),
                w.getCurrency().getCode(),
                w.getName(),
                w.isSavings(),
                w.isArchived(),
                w.getBalance(),
                w.getCreatedAt()
        );
    }
}