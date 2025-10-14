package group19.WebFinanceApp.repository;

import group19.WebFinanceApp.model.RecurringTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RecurringTemplateRepository extends JpaRepository<RecurringTemplate, Long> {
    List<RecurringTemplate> findByOwnerId(Long ownerId);
    boolean existsByOwnerIdAndNameIgnoreCase(Long ownerId, String name);
    boolean existsByOwnerIdAndNameIgnoreCaseAndIdNot(Long ownerId, String name, Long id);

    List<RecurringTemplate> findByActiveTrueAndNextRunDateLessThanEqual(LocalDate date);
}