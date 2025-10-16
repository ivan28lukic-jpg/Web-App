package group19.WebFinanceApp.repository;

import group19.WebFinanceApp.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    boolean existsByUsernameAndIdNot(String username, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);

    // Admin pretraga po username, email, imenu ili prezimenu
    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String username, String email, String firstName, String lastName, Pageable pageable
    );

    // Aktivni korisnici: imali bar 1 transakciju u poslednjem periodu
    @Query("""
           select count(distinct u.id)
           from User u
           where exists (
               select 1 from Transaction t
               where t.wallet.owner.id = u.id
                 and t.occurredAt >= :since
           )
           """)
    long countActiveSince(@Param("since") Instant since);
}