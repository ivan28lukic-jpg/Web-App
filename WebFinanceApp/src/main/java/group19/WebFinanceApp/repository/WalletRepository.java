package group19.WebFinanceApp.repository;

import group19.WebFinanceApp.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    List<Wallet> findByOwnerId(Long ownerId);
    Optional<Wallet> findByIdAndOwnerId(Long id, Long ownerId);
    boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);
    boolean existsByOwnerIdAndNameIgnoreCaseAndIdNot(Long ownerId, String name, Long id);

    List<Wallet> findByOwnerIdAndArchivedFalse(Long ownerId);
    List<Wallet> findByOwnerIdAndArchivedTrue(Long ownerId);

    List<Wallet> findByOwnerIdAndSavingsTrue(Long ownerId);
    List<Wallet> findByOwnerIdAndSavingsTrueAndArchivedFalse(Long ownerId);

    Optional<Wallet> findByIdAndOwnerIdAndArchivedFalse(Long id, Long ownerId);

    // Duplikati samo među aktivnim
    boolean existsByOwnerIdAndNameIgnoreCaseAndArchivedFalse(Long ownerId, String name);

    // Pretraga po valuti (ugneždeno polje: currency.code)
    List<Wallet> findByOwnerIdAndCurrency_CodeIgnoreCase(Long ownerId, String code);

    // Za prikaz novijih prvo
    List<Wallet> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    // Pretraga po imenu
    Optional<Wallet> findByOwnerIdAndNameIgnoreCase(Long ownerId, String name);

    // Suma svih balansa
    @Query("select coalesce(sum(w.balance), 0) from Wallet w")
    BigDecimal totalBalanceAll();

    // Suma balansa samo za korisnike koji su aktivni od 'since'
    @Query("""
           select coalesce(sum(w.balance), 0)
           from Wallet w
           where exists (
               select 1 from Transaction t
               where t.wallet.owner.id = w.owner.id
                 and t.occurredAt >= :since
           )
           """)
    BigDecimal totalBalanceForActiveUsersSince(@Param("since") Instant since);
}