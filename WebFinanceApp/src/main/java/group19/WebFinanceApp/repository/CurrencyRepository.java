package group19.WebFinanceApp.repository;

import group19.WebFinanceApp.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CurrencyRepository extends JpaRepository<Currency, Long> {

    // Case-insensitive varijante (preporučeno)
    Optional<Currency> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);

    // (Opcionalno) zadržane i exact-case varijante radi kompatibilnosti
    Optional<Currency> findByCode(String code);
    boolean existsByCode(String code);

    // Listanje uredno po kodu (za GET /api/currencies)
    List<Currency> findAllByOrderByCodeAsc();
}