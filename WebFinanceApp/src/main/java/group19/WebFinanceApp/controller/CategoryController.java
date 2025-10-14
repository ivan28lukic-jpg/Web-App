package group19.WebFinanceApp.controller;

import group19.WebFinanceApp.controller.dto.request.CategoryCreateRequest;
import group19.WebFinanceApp.controller.dto.request.CategoryUpdateRequest;
import group19.WebFinanceApp.controller.dto.response.CategoryResponse;
import group19.WebFinanceApp.model.Category;
import group19.WebFinanceApp.model.CategoryType;
import group19.WebFinanceApp.model.User;
import group19.WebFinanceApp.repository.CategoryRepository;
import group19.WebFinanceApp.repository.TransactionRepository;
import group19.WebFinanceApp.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categories;
    private final UserRepository users;
    private final TransactionRepository transactions;

    public CategoryController(CategoryRepository categories,
                              UserRepository users,
                              TransactionRepository transactions) {
        this.categories = categories;
        this.users = users;
        this.transactions = transactions;
    }

    // ====== NOVO: LIST sa filterima ?ownerId=&type=&includeArchived= ======
    @GetMapping
    public List<CategoryResponse> list(@RequestParam(required = false) Long ownerId,
                                       @RequestParam(required = false) CategoryType type,
                                       @RequestParam(defaultValue = "false") boolean includeArchived) {
        // radi kompatibilnosti sa tvojim repoom, filtriramo u memoriji
        return categories.findAll().stream()
                .filter(c -> ownerId == null || (c.getOwner() != null && c.getOwner().getId().equals(ownerId)))
                .filter(c -> type == null || c.getType() == type)
                .filter(c -> includeArchived || !c.isArchived())
                .map(this::toResponse)
                .toList();
    }

    // GLOBAL list (zadržano)
    @GetMapping("/global")
    public List<CategoryResponse> global() {
        return categories.findByOwnerIsNull().stream().map(this::toResponse).toList();
    }

    // OWNER list (zadržano) – includeGlobal=true -> vrati i globalne
    @GetMapping("/owner/{ownerId}")
    public List<CategoryResponse> byOwner(@PathVariable Long ownerId,
                                          @RequestParam(defaultValue = "true") boolean includeGlobal) {
        var list = includeGlobal
                ? categories.findAccessibleForOwner(ownerId)
                : categories.findByOwnerId(ownerId);
        return list.stream().map(this::toResponse).toList();
    }

    // GET by id
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> byId(@PathVariable Long id) {
        return categories.findById(id)
                .map(c -> ResponseEntity.ok(toResponse(c)))
                .orElseGet(() -> ResponseEntity.notFound().<CategoryResponse>build());
    }

    // CREATE (ownerId je opcionalan; ako je null -> global)
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest in) {
        User owner = null;
        if (in.getOwnerId() != null) {
            owner = users.findById(in.getOwnerId()).orElse(null);
            if (owner == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); // 404 ako nema owner-a
            }
        }

        // anti-duplikat po (owner, name, type)
        boolean duplicate = (owner == null)
                ? categories.existsByOwnerIsNullAndNameIgnoreCaseAndType(in.getName(), in.getType())
                : categories.existsByOwnerIdAndNameIgnoreCaseAndType(owner.getId(), in.getName(), in.getType());
        if (duplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409
        }

        Category c = new Category();
        c.setOwner(owner);
        c.setName(in.getName());
        c.setType(in.getType());
        c.setColor(in.getColor());
        c.setArchived(false);

        c = categories.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(c));
    }

    // UPDATE: menja name/color/archived; TYPE NE MENJAMO (uzima se iz postojeće kategorije)
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CategoryUpdateRequest in) {
        return categories.findById(id)
                .map(c -> {
                    Long ownerId = (c.getOwner() == null) ? null : c.getOwner().getId();
                    CategoryType type = c.getType(); // zadrži postojeći type

                    boolean duplicate = (ownerId == null)
                            ? categories.existsByOwnerIsNullAndNameIgnoreCaseAndTypeAndIdNot(in.getName(), type, id)
                            : categories.existsByOwnerIdAndNameIgnoreCaseAndTypeAndIdNot(ownerId, in.getName(), type, id);
                    if (duplicate) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).<CategoryResponse>build();
                    }

                    c.setName(in.getName());
                    c.setColor(in.getColor());
                    c.setArchived(in.isArchived());
                    categories.save(c);
                    return ResponseEntity.ok(toResponse(c));
                })
                .orElseGet(() -> ResponseEntity.notFound().<CategoryResponse>build());
    }

    // NOVO: PATCH /{id}/archive?archived=true|false
    @PatchMapping("/{id}/archive")
    public ResponseEntity<CategoryResponse> archive(@PathVariable Long id,
                                                    @RequestParam(defaultValue = "true") boolean archived) {
        return categories.findById(id)
                .map(c -> {
                    c.setArchived(archived);
                    categories.save(c);
                    return ResponseEntity.ok(toResponse(c));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // DELETE – 409 ako je kategorija korišćena u transakcijama
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        var opt = categories.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var c = opt.get();

        // PROVERA: postoji li transakcija koja koristi ovu kategoriju?
        if (transactions.existsByCategoryId(c.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        categories.delete(c);
        return ResponseEntity.noContent().build();
    }


    // mapper
    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(
                c.getId(),
                c.getOwner() == null ? null : c.getOwner().getId(),
                c.getName(),
                c.getType(),
                c.getColor(),
                c.isArchived(),
                c.getCreatedAt()
        );
    }
}