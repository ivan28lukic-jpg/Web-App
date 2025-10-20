package group19.WebFinanceApp.controller;

import group19.WebFinanceApp.controller.dto.request.AdminCategoryCreateRequest;
import group19.WebFinanceApp.controller.dto.request.AdminCategoryUpdateRequest;
import group19.WebFinanceApp.controller.dto.response.AdminCategoryResponse;
import group19.WebFinanceApp.model.Category;
import group19.WebFinanceApp.model.CategoryType;
import group19.WebFinanceApp.repository.CategoryRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    private final CategoryRepository categories;

    public AdminCategoryController(CategoryRepository categories) {
        this.categories = categories;
    }

    // GET /api/admin/categories?q=&type=&archived=&page=&size=&sort=
    @GetMapping
    public Page<AdminCategoryResponse> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) CategoryType type,
            @RequestParam(required = false) Boolean archived,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,ASC") String sort
    ) {
        Pageable pageable = PageRequest.of(page, size, parseSort(sort));
        return categories.adminSearchGlobal(q, type, archived, pageable)
                .map(this::toResponse);
    }

    // POST /api/admin/categories  (GLOBAL category: owner = NULL)
    @PostMapping
    public ResponseEntity<AdminCategoryResponse> create(@Valid @RequestBody AdminCategoryCreateRequest in) {
        // zaštita od duplikata (global: owner IS NULL)
        boolean exists = categories.existsByOwnerIsNullAndNameIgnoreCaseAndType(in.getName(), in.getType());
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409 CONFLICT
        }

        Category c = new Category();
        c.setOwner(null);
        c.setName(in.getName());
        c.setType(in.getType());
        c.setColor(in.getColor());
        c.setArchived(false);

        c = categories.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(c));
    }

    // PUT /api/admin/categories/{id}  (izmena naziva/boje/tipa/archived)
    @PutMapping("/{id}")
    public ResponseEntity<AdminCategoryResponse> update(@PathVariable Long id,
                                                        @Valid @RequestBody AdminCategoryUpdateRequest in) {
        return categories.findById(id)
                .map(c -> {
                    // dozvoli samo GLOBAL kategorije u adminu
                    if (c.getOwner() != null) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<AdminCategoryResponse>build();
                    }

                    String newName = in.getName();
                    CategoryType newType = in.getType() != null ? in.getType() : c.getType();

                    // provera duplikata (GLOBAL)
                    boolean duplicate = categories.existsByOwnerIsNullAndNameIgnoreCaseAndTypeAndIdNot(
                            newName, newType, c.getId());
                    if (duplicate) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).<AdminCategoryResponse>build();
                    }

                    c.setName(newName);
                    if (in.getColor() != null) c.setColor(in.getColor());
                    c.setType(newType); // ukloni ovu liniju ako ne želiš izmenu type
                    if (in.getArchived() != null) c.setArchived(in.getArchived());

                    Category saved = categories.save(c);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // PATCH /api/admin/categories/{id}/archive?archived=true|false
    @PatchMapping("/{id}/archive")
    public ResponseEntity<AdminCategoryResponse> archive(@PathVariable Long id,
                                                         @RequestParam(defaultValue = "true") boolean archived) {
        return categories.findById(id)
                .map(c -> {
                    if (c.getOwner() != null) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).<AdminCategoryResponse>build();
                    }
                    c.setArchived(archived);
                    Category saved = categories.save(c);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable Long id) {
        return categories.findById(id)
                .map(c -> {
                    if (c.getOwner() != null) {
                        // Samo globalne (admin) kategorije sme admin da briše ovde!
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                    categories.deleteById(id);
                    return ResponseEntity.noContent().build();
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
    // --- helpers ---

    private Sort parseSort(String sort) {
        try {
            String[] parts = sort.split(",", 2);
            String field = (parts.length > 0 ? parts[0] : "name");
            String dir = (parts.length > 1 ? parts[1] : "ASC");
            Sort.Direction direction = "ASC".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;

            // Dozvoljena polja za sort
            if (!field.equals("name") && !field.equals("createdAt") && !field.equals("id")) {
                field = "name";
            }
            return Sort.by(direction, field);
        } catch (Exception ignore) {
            return Sort.by(Sort.Direction.ASC, "name");
        }
    }

    private AdminCategoryResponse toResponse(Category c) {
        return new AdminCategoryResponse(
                c.getId(),
                c.getName(),
                c.getType(),
                c.getColor(),
                c.isArchived(),
                c.getCreatedAt()
        );
    }
}