package group19.WebFinanceApp.controller.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class AdminDashboardResponse {

    private long totalUsers;
    private long activeUsersLast30d;

    private BigDecimal totalBalanceAllWallets;
    private BigDecimal avgBalanceActiveUsers; // totalBalanceActiveUsers / activeUsersLast30d (0 ako nema aktivnih)

    private List<TopTransactionItem> top10Last30d;
    private List<TopTransactionItem> top10Last2m;

    public AdminDashboardResponse(long totalUsers,
                                  long activeUsersLast30d,
                                  BigDecimal totalBalanceAllWallets,
                                  BigDecimal avgBalanceActiveUsers,
                                  List<TopTransactionItem> top10Last30d,
                                  List<TopTransactionItem> top10Last2m) {
        this.totalUsers = totalUsers;
        this.activeUsersLast30d = activeUsersLast30d;
        this.totalBalanceAllWallets = totalBalanceAllWallets;
        this.avgBalanceActiveUsers = avgBalanceActiveUsers;
        this.top10Last30d = top10Last30d;
        this.top10Last2m = top10Last2m;
    }

    public long getTotalUsers() { return totalUsers; }
    public long getActiveUsersLast30d() { return activeUsersLast30d; }
    public BigDecimal getTotalBalanceAllWallets() { return totalBalanceAllWallets; }
    public BigDecimal getAvgBalanceActiveUsers() { return avgBalanceActiveUsers; }
    public List<TopTransactionItem> getTop10Last30d() { return top10Last30d; }
    public List<TopTransactionItem> getTop10Last2m() { return top10Last2m; }
}