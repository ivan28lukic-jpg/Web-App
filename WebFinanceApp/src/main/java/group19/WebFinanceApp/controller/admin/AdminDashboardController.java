package group19.WebFinanceApp.controller.admin;

import group19.WebFinanceApp.controller.dto.response.AdminDashboardResponse;
import group19.WebFinanceApp.controller.dto.response.TopTransactionItem;
import group19.WebFinanceApp.model.Transaction;
import group19.WebFinanceApp.repository.TransactionRepository;
import group19.WebFinanceApp.repository.UserRepository;
import group19.WebFinanceApp.repository.WalletRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final UserRepository users;
    private final WalletRepository wallets;
    private final TransactionRepository transactions;

    public AdminDashboardController(UserRepository users,
                                    WalletRepository wallets,
                                    TransactionRepository transactions) {
        this.users = users;
        this.wallets = wallets;
        this.transactions = transactions;
    }

    @GetMapping
    public AdminDashboardResponse summary() {
        Instant now = Instant.now();
        Instant since30d = now.minus(30, ChronoUnit.DAYS);
        Instant since2m  = now.minus(2, ChronoUnit.MINUTES);

        long totalUsers = users.count();
        long activeUsersLast30d = users.countActiveSince(since30d);

        BigDecimal totalBalanceAll = wallets.totalBalanceAll();
        if (totalBalanceAll == null) totalBalanceAll = BigDecimal.ZERO;

        BigDecimal totalBalanceActiveUsers = wallets.totalBalanceForActiveUsersSince(since30d);
        if (totalBalanceActiveUsers == null) totalBalanceActiveUsers = BigDecimal.ZERO;

        BigDecimal avgBalanceActive = activeUsersLast30d > 0
                ? totalBalanceActiveUsers.divide(BigDecimal.valueOf(activeUsersLast30d), 2, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Pageable top10 = PageRequest.of(0, 10);
        List<Transaction> top30d = transactions.topByAmountSince(since30d, top10);
        List<Transaction> top2m  = transactions.topByAmountSince(since2m,  top10);

        return new AdminDashboardResponse(
                totalUsers,
                activeUsersLast30d,
                totalBalanceAll,
                avgBalanceActive,
                top30d.stream().map(this::toTopItem).toList(),
                top2m.stream().map(this::toTopItem).toList()
        );
    }

    private TopTransactionItem toTopItem(Transaction t) {
        Long ownerId = t.getWallet() != null && t.getWallet().getOwner() != null
                ? t.getWallet().getOwner().getId()
                : null;

        return new TopTransactionItem(
                t.getId(),
                ownerId,
                t.getWallet() != null ? t.getWallet().getId() : null,
                t.getCategory() != null ? t.getCategory().getId() : null,
                t.getCategory() != null ? t.getCategory().getName() : null,
                t.getCategory() != null ? t.getCategory().getType() : null,
                t.getAmount(),
                t.getDescription(),
                t.getOccurredAt()
        );
    }
}