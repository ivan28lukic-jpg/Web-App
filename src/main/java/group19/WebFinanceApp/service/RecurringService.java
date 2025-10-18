package group19.WebFinanceApp.service;

import group19.WebFinanceApp.controller.dto.request.RecurringTemplateCreateRequest;
import group19.WebFinanceApp.controller.dto.request.RecurringTemplateUpdateRequest;
import group19.WebFinanceApp.model.*;
import group19.WebFinanceApp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RecurringService {

    private final RecurringTemplateRepository templates;
    private final RecurringInstanceRepository instances;
    private final UserRepository users;
    private final WalletRepository wallets;
    private final CategoryRepository categories;
    private final TransactionRepository transactions;
    private LocalDate computeFirstFutureRunDate(RecurringTemplate t) {
        LocalDate today = LocalDate.now();
        LocalDate next = t.getStartDate();
        while (next != null && next.isBefore(today)) {
            next = nextDate(t, next);
        }
        return next;
    }
    public RecurringService(RecurringTemplateRepository templates,
                            RecurringInstanceRepository instances,
                            UserRepository users,
                            WalletRepository wallets,
                            CategoryRepository categories,
                            TransactionRepository transactions) {
        this.templates = templates;
        this.instances = instances;
        this.users = users;
        this.wallets = wallets;
        this.categories = categories;
        this.transactions = transactions;
    }

    /* =============== CRUD TEMPLATE =============== */

    @Transactional
    public RecurringTemplate create(RecurringTemplateCreateRequest in) {
        var owner = users.findById(in.getOwnerId()).orElseThrow();
        var wallet = wallets.findById(in.getWalletId()).orElseThrow();
        var category = categories.findById(in.getCategoryId()).orElseThrow();

        // wallet mora pripadati owner-u
        if (!wallet.getOwner().getId().equals(owner.getId())) {
            throw new IllegalArgumentException("Wallet does not belong to owner");
        }
        // jedinstveno ime po vlasniku
        if (templates.existsByOwnerIdAndNameIgnoreCase(owner.getId(), in.getName())) {
            throw new IllegalStateException("Template name already exists for this owner");
        }

        RecurringTemplate t = new RecurringTemplate();
        t.setOwner(owner);
        t.setWallet(wallet);
        t.setCategory(category);
        t.setName(in.getName());
        t.setAmount(in.getAmount());
        t.setDescriptionTemplate(in.getDescriptionTemplate());
        t.setFrequency(in.getFrequency());
        t.setInterval(in.getInterval());
        t.setStartDate(in.getStartDate());
        t.setEndDate(in.getEndDate());
        t.setActive(in.getActive() == null ? true : in.getActive());
        t.setNextRunDate(computeFirstFutureRunDate(t));
        return templates.save(t);
    }

    @Transactional
    public Optional<RecurringTemplate> update(Long id, RecurringTemplateUpdateRequest in) {
        return templates.findById(id).map(t -> {
            if (templates.existsByOwnerIdAndNameIgnoreCaseAndIdNot(t.getOwner().getId(), in.getName(), id)) {
                throw new IllegalStateException("Template name already exists for this owner");
            }

            var wallet = wallets.findById(in.getWalletId()).orElseThrow();
            if (!wallet.getOwner().getId().equals(t.getOwner().getId())) {
                throw new IllegalArgumentException("Wallet does not belong to owner");
            }
            var category = categories.findById(in.getCategoryId()).orElseThrow();

            t.setWallet(wallet);
            t.setCategory(category);
            t.setName(in.getName());
            t.setAmount(in.getAmount());
            t.setDescriptionTemplate(in.getDescriptionTemplate());
            t.setFrequency(in.getFrequency());
            t.setInterval(in.getInterval());
            t.setStartDate(in.getStartDate());
            t.setEndDate(in.getEndDate());
            t.setActive(in.isActive());

            // ako je next pre starta – resetuj; ako je posle end – null
            if (t.getNextRunDate() == null || t.getNextRunDate().isBefore(t.getStartDate())) {
                t.setNextRunDate(t.getStartDate());
            }
            if (t.getEndDate() != null && t.getNextRunDate() != null && t.getNextRunDate().isAfter(t.getEndDate())) {
                t.setNextRunDate(null);
            }

            return templates.save(t);
        });
    }

    @Transactional
    public Optional<RecurringTemplate> toggle(Long id, boolean active) {
        return templates.findById(id).map(t -> {
            t.setActive(active);
            // ako se reaktivira a next je null -> postavi na max(today, startDate)
            if (active && t.getNextRunDate() == null) {
                LocalDate base = LocalDate.now();
                if (base.isBefore(t.getStartDate())) base = t.getStartDate();
                t.setNextRunDate(base);
            }
            return templates.save(t);
        });
    }

    public List<RecurringTemplate> listByOwner(Long ownerId) {
        return templates.findByOwnerId(ownerId);
    }

    /* =============== PREVIEW =============== */

    public List<LocalDate> preview(Long templateId, LocalDate from, LocalDate to) {
        RecurringTemplate t = templates.findById(templateId).orElseThrow();
        if (from == null) from = t.getStartDate();
        if (to == null)   to   = t.getEndDate() != null ? t.getEndDate() : from.plusYears(1);

        List<LocalDate> days = new ArrayList<>();
        LocalDate cursor = alignToStart(t, from);
        while (cursor != null && !cursor.isAfter(to)) {
            if ((t.getEndDate() == null || !cursor.isAfter(t.getEndDate()))
                    && !cursor.isBefore(t.getStartDate())) {
                days.add(cursor);
            }
            cursor = nextDate(t, cursor);
        }
        return days;
    }

    /* =============== EXECUTION =============== */

    @Transactional
    public List<Transaction> runDue() {
        LocalDate today = LocalDate.now();
        List<RecurringTemplate> due = templates.findByActiveTrueAndNextRunDateLessThanEqual(today);
        List<Transaction> created = new ArrayList<>();
        for (RecurringTemplate t : due) {
            LocalDate cursor = t.getNextRunDate();
            while (cursor != null && !cursor.isAfter(today)) {
                Transaction tx = runOnce(t, cursor);
                if (tx != null) created.add(tx);
                cursor = t.getNextRunDate(); // posle runOnce se ažurira na sledeći
            }
        }
        return created;
    }

    @Transactional
    public Optional<Transaction> runNow(Long templateId) {
        return templates.findById(templateId).map(t -> {
            LocalDate date = (t.getNextRunDate() != null ? t.getNextRunDate() : LocalDate.now());
            return runOnce(t, date);
        });
    }

    /* ===== Helpers ===== */

    private Transaction runOnce(RecurringTemplate t, LocalDate date) {
        if (!t.isActive()) return null;
        if (t.getEndDate() != null && date.isAfter(t.getEndDate())) {
            t.setNextRunDate(null);
            templates.save(t);
            return null;
        }

        String key = date.toString(); // yyyy-MM-dd
        if (instances.existsByTemplateIdAndPeriodKey(t.getId(), key)) {
            // već izvršeno – samo pomeri next
            t.setNextRunDate(nextDate(t, date));
            templates.save(t);
            return null;
        }

        // kreiraj transakciju
        Wallet w = t.getWallet();
        Category c = t.getCategory();

        Transaction tx = new Transaction();
        tx.setWallet(w);
        tx.setCategory(c);
        tx.setAmount(t.getAmount());
        tx.setDescription(buildDescription(t, date));
        tx.setOccurredAt(date.atStartOfDay(ZoneOffset.UTC).toInstant());
        tx.setTransferId(null);

        // ažuriraj balans novčanika
        BigDecimal delta = (c.getType() == CategoryType.INCOME) ? t.getAmount() : t.getAmount().negate();
        w.setBalance(w.getBalance().add(delta));
        wallets.save(w);

        tx = transactions.save(tx);

        // upiši instance (idempotencija)
        instances.save(new RecurringInstance(t, key, Instant.now()));

        // pomeri nextRunDate
        t.setNextRunDate(nextDate(t, date));
        templates.save(t);

        return tx;
    }

    private String buildDescription(RecurringTemplate t, LocalDate date) {
        String base = (t.getDescriptionTemplate() == null || t.getDescriptionTemplate().isBlank())
                ? t.getName()
                : t.getDescriptionTemplate();
        return base + " (" + date + ")";
    }

    private LocalDate alignToStart(RecurringTemplate t, LocalDate from) {
        // poravnaj prvi datum >= from po frekvenciji
        LocalDate cursor = t.getStartDate();
        if (cursor.isAfter(from)) return cursor;
        while (!cursor.isAfter(from)) {
            LocalDate next = nextDate(t, cursor);
            if (next == null || next.isAfter(from)) break;
            cursor = next;
        }
        return cursor.isBefore(from) ? nextDate(t, cursor) : cursor;
    }

    private LocalDate nextDate(RecurringTemplate t, LocalDate current) {
        if (t.getInterval() < 1) return null;
        LocalDate next;
        switch (t.getFrequency()) {
            case DAILY -> next = current.plusDays(t.getInterval());
            case WEEKLY -> next = current.plusWeeks(t.getInterval());
            case MONTHLY -> next = current.plusMonths(t.getInterval());
            case YEARLY -> next = current.plusYears(t.getInterval());
            default -> next = null;
        }
        if (t.getEndDate() != null && next != null && next.isAfter(t.getEndDate())) {
            return null;
        }
        return next;
    }
}