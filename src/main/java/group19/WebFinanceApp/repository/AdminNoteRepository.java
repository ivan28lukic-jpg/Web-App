package group19.WebFinanceApp.repository;

import group19.WebFinanceApp.model.AdminNote;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdminNoteRepository extends JpaRepository<AdminNote, Long> {

    Page<AdminNote> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}