package group19.WebFinanceApp.repository;

import group19.WebFinanceApp.model.Category;
import group19.WebFinanceApp.model.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Sve kategorije koje korisnik "vidi": njegove + globalne
    @Query("select c from Category c where c.owner.id = :ownerId or c.owner is null")
    List<Category> findAccessibleForOwner(Long ownerId);

    List<Category> findByOwnerId(Long ownerId);
    List<Category> findByOwnerIsNull();
    List<Category> findByOwnerIdAndType(Long ownerId, CategoryType type);

    boolean existsByOwnerIdAndNameIgnoreCaseAndType(Long ownerId, String name, CategoryType type);
    boolean existsByOwnerIsNullAndNameIgnoreCaseAndType(String name, CategoryType type);

    boolean existsByOwnerIdAndNameIgnoreCaseAndTypeAndIdNot(Long ownerId, String name, CategoryType type, Long id);
    boolean existsByOwnerIsNullAndNameIgnoreCaseAndTypeAndIdNot(String name, CategoryType type, Long id);

    // NOVO: za pronalazak fallback kategorija ("Transfer Out"/"Transfer In")
    Optional<Category> findByOwnerIdAndNameIgnoreCaseAndType(Long ownerId, String name, CategoryType type);
    Optional<Category> findByOwnerIsNullAndNameIgnoreCaseAndType(String name, CategoryType type);

    @Query("""
           SELECT c
           FROM Category c
           WHERE c.owner IS NULL
             AND (:q IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')))
             AND (:type IS NULL OR c.type = :type)
             AND (:archived IS NULL OR c.archived = :archived)
           """)
    Page<Category> adminSearchGlobal(@Param("q") String q,
                                     @Param("type") group19.WebFinanceApp.model.CategoryType type,
                                     @Param("archived") Boolean archived,
                                     Pageable pageable);
}