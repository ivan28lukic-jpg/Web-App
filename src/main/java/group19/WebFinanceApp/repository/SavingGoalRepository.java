package group19.WebFinanceApp.repository;

import group19.WebFinanceApp.model.SavingGoal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavingGoalRepository extends JpaRepository<SavingGoal, Long> {
    List<SavingGoal> findByOwnerId(Long ownerId);
    boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);
    boolean existsByOwnerIdAndNameIgnoreCaseAndIdNot(Long ownerId, String name, Long id);

    // Korisno za listanja bez ručnog filtriranja
    List<SavingGoal> findByOwnerIdAndArchivedFalse(Long ownerId);
    List<SavingGoal> findByOwnerIdAndArchivedTrue(Long ownerId);

    // Pomoćne pretrage
    List<SavingGoal> findByWalletId(Long walletId);
    Optional<SavingGoal> findByIdAndOwnerId(Long id, Long ownerId);
}