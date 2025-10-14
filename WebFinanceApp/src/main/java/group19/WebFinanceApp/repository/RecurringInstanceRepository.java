package group19.WebFinanceApp.repository;

import group19.WebFinanceApp.model.RecurringInstance;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecurringInstanceRepository extends JpaRepository<RecurringInstance, Long> {
    boolean existsByTemplateIdAndPeriodKey(Long templateId, String periodKey);
}