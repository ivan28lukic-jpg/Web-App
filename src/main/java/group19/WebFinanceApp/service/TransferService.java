package group19.WebFinanceApp.service;

import group19.WebFinanceApp.model.*;
import group19.WebFinanceApp.repository.CategoryRepository;
import group19.WebFinanceApp.repository.CurrencyRepository;
import group19.WebFinanceApp.repository.TransactionRepository;
import group19.WebFinanceApp.repository.WalletRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransferService {

    private final WalletRepository wallets;
    private final TransactionRepository transactions;
    private final CategoryRepository categories;
    private final CurrencyRepository currencies;

    public TransferService(WalletRepository wallets,
                           TransactionRepository transactions,
                           CategoryRepository categories,
                           CurrencyRepository currencies) {
        this.wallets = wallets;
        this.transactions = transactions;
        this.categories = categories;
        this.currencies = currencies;
    }

    public record TransferResult(Transaction outTx, Transaction inTx) {}

    /**
     * Izvršava transfer između novčanika — sa automatskom FX konverzijom ako su valute različite.
     * amountFrom je iznos u valuti “from” novčanika. Ako su valute različite, amountTo se obračunava.
     */
    @Transactional
    public TransferResult transfer(Long fromWalletId,
                                   Long toWalletId,
                                   BigDecimal amountFrom,
                                   String description,
                                   Instant occurredAt,
                                   Long outCategoryId,
                                   Long inCategoryId) {

        if (fromWalletId == null || toWalletId == null || amountFrom == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fromWalletId, toWalletId i amount su obavezni");
        }
        if (fromWalletId.equals(toWalletId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Izvorni i odredišni wallet moraju biti različiti");
        }
        if (amountFrom.compareTo(new BigDecimal("0.01")) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimalni iznos je 0.01");
        }

        Wallet from = wallets.findById(fromWalletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Izvorni wallet ne postoji"));
        Wallet to = wallets.findById(toWalletId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Odredišni wallet ne postoji"));

        if (from.isArchived() || to.isArchived()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ne može transfer na/sa arhiviranog wallet-a");
        }
        if (!from.getOwner().getId().equals(to.getOwner().getId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Transfer samo unutar istog vlasnika");
        }
        if (from.getBalance().compareTo(amountFrom) < 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Nedovoljno sredstava na izvornom wallet-u");
        }

        // Kategorije (ako nisu prosleđene, pokušaj da pronađeš “Transfer Out/Transfer In”)
        Category outCat = resolveCategory(from.getOwner().getId(), outCategoryId, "Transfer Out", CategoryType.EXPENSE);
        Category inCat  = resolveCategory(to.getOwner().getId(),   inCategoryId,  "Transfer In",  CategoryType.INCOME);

        Instant when = (occurredAt != null) ? occurredAt : Instant.now();
        String transferId = UUID.randomUUID().toString();

        // FX obračun (amountTo je u valuti odredišnog wallet-a)
        BigDecimal amountTo = sameCurrency(from.getCurrency(), to.getCurrency())
                ? amountFrom
                : fxConvert(amountFrom, from.getCurrency(), to.getCurrency());

        // Kreiraj OUT (expense) i IN (income)
        Transaction tOut = new Transaction();
        tOut.setWallet(from);
        tOut.setCategory(outCat);
        tOut.setAmount(scale2(amountFrom));
        tOut.setDescription(description);
        tOut.setOccurredAt(when);
        tOut.setTransferId(transferId);

        Transaction tIn = new Transaction();
        tIn.setWallet(to);
        tIn.setCategory(inCat);
        tIn.setAmount(scale2(amountTo));
        tIn.setDescription(description);
        tIn.setOccurredAt(when);
        tIn.setTransferId(transferId);

        // Ažuriraj stanja
        from.setBalance(scale2(from.getBalance().subtract(amountFrom)));
        to.setBalance(scale2(to.getBalance().add(amountTo)));
        wallets.save(from);
        wallets.save(to);

        // Sačuvaj transakcije
        tOut = transactions.save(tOut);
        tIn  = transactions.save(tIn);

        return new TransferResult(tOut, tIn);
    }

    /* ========== helpers ========== */

    private boolean sameCurrency(Currency a, Currency b) {
        return a.getId().equals(b.getId());
    }

    /**
     * valueVsEur znači: koliko JEDAN EUR vredi u toj valuti.
     * Konverzija:  amount_from / rate_from * rate_to
     */
    private BigDecimal fxConvert(BigDecimal amountFrom, Currency from, Currency to) {
        Currency fromC = currencies.findById(from.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nepoznata valuta FROM"));
        Currency toC = currencies.findById(to.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nepoznata valuta TO"));

        // amountTo = amountFrom / from.rate * to.rate
        BigDecimal amountTo = amountFrom
                .divide(fromC.getValueVsEur(), 10, RoundingMode.HALF_UP)
                .multiply(toC.getValueVsEur());

        return scale2(amountTo);
    }

    private BigDecimal scale2(BigDecimal x) {
        return x.setScale(2, RoundingMode.HALF_UP);
    }

    private Category resolveCategory(Long ownerId, Long explicitId, String fallbackName, CategoryType type) {
        if (explicitId != null) {
            Category c = categories.findById(explicitId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nepostojeća categoryId: " + explicitId));
            if (c.getType() != type) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pogrešan tip kategorije za transfer (" + type + ")");
            }
            return c;
        }
        // pokušaj vlasničku “fallback” kategoriju
        return categories.findByOwnerIdAndNameIgnoreCaseAndType(ownerId, fallbackName, type)
                .or(() -> categories.findByOwnerIsNullAndNameIgnoreCaseAndType(fallbackName, type))
                .orElseGet(() -> {
                    Category n = new Category();
                    n.setOwner(null);              // global fallback (ne traži User repo)
                    n.setName(fallbackName);
                    n.setType(type);
                    n.setArchived(false);
                    n.setPredefined(true);
                    n.setCreatedAt(Instant.now());
                    return categories.save(n);
                });
    }
}