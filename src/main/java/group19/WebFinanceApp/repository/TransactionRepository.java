package group19.WebFinanceApp.repository;

import group19.WebFinanceApp.model.Transaction;
import group19.WebFinanceApp.model.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByWalletIdOrderByOccurredAtDesc(Long walletId);

    Optional<Transaction> findFirstByTransferIdAndIdNot(String transferId, Long excludeId);

    // Lista transakcija za dati wallet
    List<Transaction> findByWalletId(Long walletId);

    // Za dohvat jedne transakcije unutar konkretnog wallet-a
    Optional<Transaction> findByIdAndWalletId(Long id, Long walletId);

    // Sve transakcije korisnika (preko owner_id iz Wallet-a)
    List<Transaction> findByWalletOwnerId(Long ownerId);

    // Filtriranje po datumu (npr. za izveštaje)
    List<Transaction> findByWalletIdAndOccurredAtBetween(Long walletId, Instant from, Instant to);

    // Za parove transfera (ako budeš pravio logiku transferId)
    List<Transaction> findByTransferId(String transferId);

    List<Transaction> findByTransferIdIsNotNull();

    boolean existsByTransferId(String transferId);
    List<Transaction> findByTransferIdOrderByOccurredAtAsc(String transferId);
    boolean existsByCategoryId(Long categoryId);

    Optional<Transaction> findFirstByTransferIdAndCategory_Type(
            String transferId, group19.WebFinanceApp.model.CategoryType type);

    @Query("""
       select t from Transaction t
       where t.transferId is not null
         and t.category.type = group19.WebFinanceApp.model.CategoryType.EXPENSE
         and (:from is null or t.occurredAt >= :from)
         and (:to   is null or t.occurredAt <= :to)
         and (:walletId is null
              or exists (
                    select 1 from Transaction t2
                    where t2.transferId = t.transferId
                      and t2.wallet.id = :walletId
              )
         )
       """)
    Page<Transaction> findTransferOutsInvolvingWallet(
            @Param("walletId") Long walletId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            Pageable pageable
    );

    @Query("""
      select t from Transaction t
      join t.category c
      join t.wallet   w
      where (:walletId  is null or w.id = :walletId)
        and (:ownerId   is null or w.owner.id = :ownerId)
        and (:categoryId is null or c.id = :categoryId)
        and (:type      is null or c.type = :type)
        and (:from      is null or t.occurredAt >= :from)
        and (:to        is null or t.occurredAt <= :to)
        and (:minAmount is null or t.amount >= :minAmount)
        and (:maxAmount is null or t.amount <= :maxAmount)
        and (:q         is null or lower(t.description) like lower(concat('%', :q, '%')))
    """)
    Page<Transaction> search(@Param("walletId") Long walletId,
                             @Param("ownerId") Long ownerId,
                             @Param("categoryId") Long categoryId,
                             @Param("type") CategoryType type,
                             @Param("from") Instant from,
                             @Param("to") Instant to,
                             @Param("minAmount") BigDecimal minAmount,
                             @Param("maxAmount") BigDecimal maxAmount,
                             @Param("q") String q,
                             Pageable pageable);

    /* =====================  TOP KATEGORIJE (TROŠKOVI)  ===================== */

    interface TopCategoryRow {
        Long getCategoryId();
        String getCategoryName();
        BigDecimal getTotalAmount();
    }

    @Query("""
        select c.id as categoryId,
               c.name as categoryName,
               COALESCE(sum(t.amount), 0) as totalAmount
        from Transaction t
        join t.category c
        join t.wallet   w
        where c.type = group19.WebFinanceApp.model.CategoryType.EXPENSE
          and (:ownerId  is null or w.owner.id = :ownerId)
          and (:walletId is null or w.id = :walletId)
          and (:from     is null or t.occurredAt >= :from)
          and (:to       is null or t.occurredAt <= :to)
        group by c.id, c.name
        order by sum(t.amount) desc
    """)
    List<TopCategoryRow> topExpenseCategories(@Param("ownerId") Long ownerId,
                                              @Param("walletId") Long walletId,
                                              @Param("from") Instant from,
                                              @Param("to") Instant to,
                                              Pageable pageable);

    @Query("""
        select coalesce(sum(t.amount), 0)
        from Transaction t
        join t.category c
        join t.wallet   w
        where c.type = group19.WebFinanceApp.model.CategoryType.EXPENSE
          and (:ownerId  is null or w.owner.id = :ownerId)
          and (:walletId is null or w.id = :walletId)
          and (:from     is null or t.occurredAt >= :from)
          and (:to       is null or t.occurredAt <= :to)
    """)
    BigDecimal totalExpense(@Param("ownerId") Long ownerId,
                            @Param("walletId") Long walletId,
                            @Param("from") Instant from,
                            @Param("to") Instant to);

    /* =====================  STATISTIKA PO PERIODIMA  ===================== */

    interface PeriodStatRow {
        String getPeriod();
        BigDecimal getIncome();
        BigDecimal getExpense();
        BigDecimal getNet();
    }

    @Query(value = """
        SELECT
          FORMATDATETIME(t.occurred_at, 'yyyy-MM-dd') AS period,
          SUM(CASE WHEN c.type = 'INCOME'  THEN t.amount ELSE 0 END) AS income,
          SUM(CASE WHEN c.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS expense,
          SUM(CASE WHEN c.type = 'INCOME'  THEN  t.amount
                   WHEN c.type = 'EXPENSE' THEN -t.amount
                   ELSE 0 END) AS net
        FROM transactions t
        JOIN categories c ON c.id = t.category_id
        JOIN wallets    w ON w.id = t.wallet_id
        WHERE (:walletId IS NULL OR t.wallet_id = :walletId)
          AND (:ownerId  IS NULL OR w.owner_id  = :ownerId)
          AND (:type     IS NULL OR c.type      = :type)
          AND (:fromTs   IS NULL OR t.occurred_at >= :fromTs)
          AND (:toTs     IS NULL OR t.occurred_at <= :toTs)
        GROUP BY FORMATDATETIME(t.occurred_at, 'yyyy-MM-dd')
        ORDER BY FORMATDATETIME(t.occurred_at, 'yyyy-MM-dd')
    """, nativeQuery = true)
    List<PeriodStatRow> statsDaily(@Param("walletId") Long walletId,
                                   @Param("ownerId") Long ownerId,
                                   @Param("type") CategoryType type,
                                   @Param("fromTs") Instant from,
                                   @Param("toTs") Instant to);

    @Query(value = """
        SELECT
          FORMATDATETIME(t.occurred_at, 'YYYY-ww') AS period,
          SUM(CASE WHEN c.type = 'INCOME'  THEN t.amount ELSE 0 END) AS income,
          SUM(CASE WHEN c.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS expense,
          SUM(CASE WHEN c.type = 'INCOME'  THEN  t.amount
                   WHEN c.type = 'EXPENSE' THEN -t.amount
                   ELSE 0 END) AS net
        FROM transactions t
        JOIN categories c ON c.id = t.category_id
        JOIN wallets    w ON w.id = t.wallet_id
        WHERE (:walletId IS NULL OR t.wallet_id = :walletId)
          AND (:ownerId  IS NULL OR w.owner_id  = :ownerId)
          AND (:type     IS NULL OR c.type      = :type)
          AND (:fromTs   IS NULL OR t.occurred_at >= :fromTs)
          AND (:toTs     IS NULL OR t.occurred_at <= :toTs)
        GROUP BY FORMATDATETIME(t.occurred_at, 'YYYY-ww')
        ORDER BY FORMATDATETIME(t.occurred_at, 'YYYY-ww')
    """, nativeQuery = true)
    List<PeriodStatRow> statsWeekly(@Param("walletId") Long walletId,
                                    @Param("ownerId") Long ownerId,
                                    @Param("type") CategoryType type,
                                    @Param("fromTs") Instant from,
                                    @Param("toTs") Instant to);

    @Query(value = """
        SELECT
          FORMATDATETIME(t.occurred_at, 'yyyy-MM') AS period,
          SUM(CASE WHEN c.type = 'INCOME'  THEN t.amount ELSE 0 END) AS income,
          SUM(CASE WHEN c.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS expense,
          SUM(CASE WHEN c.type = 'INCOME'  THEN  t.amount
                   WHEN c.type = 'EXPENSE' THEN -t.amount
                   ELSE 0 END) AS net
        FROM transactions t
        JOIN categories c ON c.id = t.category_id
        JOIN wallets    w ON w.id = t.wallet_id
        WHERE (:walletId IS NULL OR t.wallet_id = :walletId)
          AND (:ownerId  IS NULL OR w.owner_id  = :ownerId)
          AND (:type     IS NULL OR c.type      = :type)
          AND (:fromTs   IS NULL OR t.occurred_at >= :fromTs)
          AND (:toTs     IS NULL OR t.occurred_at <= :toTs)
        GROUP BY FORMATDATETIME(t.occurred_at, 'yyyy-MM')
        ORDER BY FORMATDATETIME(t.occurred_at, 'yyyy-MM')
    """, nativeQuery = true)
    List<PeriodStatRow> statsMonthly(@Param("walletId") Long walletId,
                                     @Param("ownerId") Long ownerId,
                                     @Param("type") CategoryType type,
                                     @Param("fromTs") Instant from,
                                     @Param("toTs") Instant to);

    @Query(value = """
        SELECT
          FORMATDATETIME(t.occurred_at, 'yyyy') AS period,
          SUM(CASE WHEN c.type = 'INCOME'  THEN t.amount ELSE 0 END) AS income,
          SUM(CASE WHEN c.type = 'EXPENSE' THEN t.amount ELSE 0 END) AS expense,
          SUM(CASE WHEN c.type = 'INCOME'  THEN  t.amount
                   WHEN c.type = 'EXPENSE' THEN -t.amount
                   ELSE 0 END) AS net
        FROM transactions t
        JOIN categories c ON c.id = t.category_id
        JOIN wallets    w ON w.id = t.wallet_id
        WHERE (:walletId IS NULL OR t.wallet_id = :walletId)
          AND (:ownerId  IS NULL OR w.owner_id  = :ownerId)
          AND (:type     IS NULL OR c.type      = :type)
          AND (:fromTs   IS NULL OR t.occurred_at >= :fromTs)
          AND (:toTs     IS NULL OR t.occurred_at <= :toTs)
        GROUP BY FORMATDATETIME(t.occurred_at, 'yyyy')
        ORDER BY FORMATDATETIME(t.occurred_at, 'yyyy')
    """, nativeQuery = true)
    List<PeriodStatRow> statsYearly(@Param("walletId") Long walletId,
                                    @Param("ownerId") Long ownerId,
                                    @Param("type") CategoryType type,
                                    @Param("fromTs") Instant from,
                                    @Param("toTs") Instant to);

    interface PeriodAgg {
        String getPeriod();
        BigDecimal getIncome();
        BigDecimal getExpense();
    }

    @Query(value = """
    select 
      FORMATDATETIME(t.occurred_at, 'YYYY-ww') as period,
      COALESCE(SUM(case when c.type='INCOME'  then t.amount else 0 end), 0) as income,
      COALESCE(SUM(case when c.type='EXPENSE' then t.amount else 0 end), 0) as expense
    from transactions t
    join categories c on c.id = t.category_id
    join wallets    w on w.id = t.wallet_id
    where (:ownerId   is null or w.owner_id = :ownerId)
      and (:walletId  is null or t.wallet_id = :walletId)
      and (:categoryId is null or t.category_id = :categoryId)
      and (:fromTs    is null or t.occurred_at >= :fromTs)
      and (:toTs      is null or t.occurred_at <= :toTs)
    group by FORMATDATETIME(t.occurred_at, 'YYYY-ww')
    order by period
    """, nativeQuery = true)
    List<PeriodAgg> statsWeekly(@Param("ownerId") Long ownerId,
                                @Param("walletId") Long walletId,
                                @Param("categoryId") Long categoryId,
                                @Param("fromTs") Instant fromTs,
                                @Param("toTs") Instant toTs);

    @Query(value = """
    select 
      FORMATDATETIME(t.occurred_at, 'yyyy-MM') as period,
      COALESCE(SUM(case when c.type='INCOME'  then t.amount else 0 end), 0) as income,
      COALESCE(SUM(case when c.type='EXPENSE' then t.amount else 0 end), 0) as expense
    from transactions t
    join categories c on c.id = t.category_id
    join wallets    w on w.id = t.wallet_id
    where (:ownerId   is null or w.owner_id = :ownerId)
      and (:walletId  is null or t.wallet_id = :walletId)
      and (:categoryId is null or t.category_id = :categoryId)
      and (:fromTs    is null or t.occurred_at >= :fromTs)
      and (:toTs      is null or t.occurred_at <= :toTs)
    group by FORMATDATETIME(t.occurred_at, 'yyyy-MM')
    order by period
    """, nativeQuery = true)
    List<PeriodAgg> statsMonthly(@Param("ownerId") Long ownerId,
                                 @Param("walletId") Long walletId,
                                 @Param("categoryId") Long categoryId,
                                 @Param("fromTs") Instant fromTs,
                                 @Param("toTs") Instant toTs);

    @Query(value = """
    select 
      CAST(EXTRACT(YEAR FROM t.occurred_at) AS VARCHAR) || '-Q' ||
      CAST(FLOOR( (EXTRACT(MONTH FROM t.occurred_at) + 2) / 3 ) AS VARCHAR) as period,
      COALESCE(SUM(case when c.type='INCOME'  then t.amount else 0 end), 0) as income,
      COALESCE(SUM(case when c.type='EXPENSE' then t.amount else 0 end), 0) as expense
    from transactions t
    join categories c on c.id = t.category_id
    join wallets    w on w.id = t.wallet_id
    where (:ownerId   is null or w.owner_id = :ownerId)
      and (:walletId  is null or t.wallet_id = :walletId)
      and (:categoryId is null or t.category_id = :categoryId)
      and (:fromTs    is null or t.occurred_at >= :fromTs)
      and (:toTs      is null or t.occurred_at <= :toTs)
    group by 
      CAST(EXTRACT(YEAR FROM t.occurred_at) AS VARCHAR) || '-Q' ||
      CAST(FLOOR( (EXTRACT(MONTH FROM t.occurred_at) + 2) / 3 ) AS VARCHAR)
    order by period
    """, nativeQuery = true)
    List<PeriodAgg> statsQuarterly(@Param("ownerId") Long ownerId,
                                   @Param("walletId") Long walletId,
                                   @Param("categoryId") Long categoryId,
                                   @Param("fromTs") Instant fromTs,
                                   @Param("toTs") Instant toTs);

    @Query(value = """
    select 
      CAST(EXTRACT(YEAR FROM t.occurred_at) AS VARCHAR) as period,
      COALESCE(SUM(case when c.type='INCOME'  then t.amount else 0 end), 0) as income,
      COALESCE(SUM(case when c.type='EXPENSE' then t.amount else 0 end), 0) as expense
    from transactions t
    join categories c on c.id = t.category_id
    join wallets    w on w.id = t.wallet_id
    where (:ownerId   is null or w.owner_id = :ownerId)
      and (:walletId  is null or t.wallet_id = :walletId)
      and (:categoryId is null or t.category_id = :categoryId)
      and (:fromTs    is null or t.occurred_at >= :fromTs)
      and (:toTs      is null or t.occurred_at <= :toTs)
    group by CAST(EXTRACT(YEAR FROM t.occurred_at) AS VARCHAR)
    order by period
    """, nativeQuery = true)
    List<PeriodAgg> statsYearly(@Param("ownerId") Long ownerId,
                                @Param("walletId") Long walletId,
                                @Param("categoryId") Long categoryId,
                                @Param("fromTs") Instant fromTs,
                                @Param("toTs") Instant toTs);

    @Query("""
           SELECT t
           FROM Transaction t
           WHERE t.wallet.owner.id = :ownerId
             AND (:from IS NULL OR t.occurredAt >= :from)
             AND (:to   IS NULL OR t.occurredAt <= :to)
             AND (:categoryId IS NULL OR t.category.id = :categoryId)
             AND (:minAmount IS NULL OR t.amount >= :minAmount)
             AND (:maxAmount IS NULL OR t.amount <= :maxAmount)
           """)
    Page<Transaction> adminFindByOwnerFilters(
            @Param("ownerId") Long ownerId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("categoryId") Long categoryId,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable
    );
    @Query("""
        select t from Transaction t
        join t.category c
        join t.wallet   w
        where c.type = group19.WebFinanceApp.model.CategoryType.EXPENSE
          and (:ownerId   is null or w.owner.id = :ownerId)
          and (:walletId  is null or w.id = :walletId)
          and (:categoryId is null or c.id = :categoryId)
          and (:from     is null or t.occurredAt >= :from)
          and (:to       is null or t.occurredAt <= :to)
          and (:minAmount is null or t.amount >= :minAmount)
          and (:maxAmount is null or t.amount <= :maxAmount)
        order by t.amount desc
    """)
    List<Transaction> topExpenses(
            @Param("ownerId") Long ownerId,
            @Param("walletId") Long walletId,
            @Param("categoryId") Long categoryId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            Pageable pageable
    );

    // TOP po iznosu od zadatog vremena (koristi se za 30d i 2m)
    @Query("""
           select t from Transaction t
           where t.occurredAt >= :since
           order by t.amount desc
           """)
    List<Transaction> topByAmountSince(@Param("since") Instant since, Pageable pageable);

    @Query("""
  select t from Transaction t
  join t.category c
  join t.wallet w
  join w.owner u
  where (:walletId is null or w.id = :walletId)
    and (:ownerUsername is null or lower(u.username) = lower(:ownerUsername))
    and (:categoryName is null or lower(c.name) = lower(:categoryName))
    and (:categoryId is null or c.id = :categoryId)
    and (:type is null or c.type = :type)
    and (:from is null or t.occurredAt >= :from)
    and (:to is null or t.occurredAt <= :to)
    and (:minAmount is null or t.amount >= :minAmount)
    and (:maxAmount is null or t.amount <= :maxAmount)
    and (:q is null or lower(t.description) like lower(concat('%', :q, '%')))
""")
    Page<Transaction> adminSearch(
            @Param("walletId") Long walletId,
            @Param("ownerUsername") String ownerUsername,
            @Param("categoryName") String categoryName,
            @Param("categoryId") Long categoryId,
            @Param("type") CategoryType type,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("q") String q,
            Pageable pageable
    );
}
