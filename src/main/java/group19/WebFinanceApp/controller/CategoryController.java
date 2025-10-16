package group19.WebFinanceApp.controller;

import group19.WebFinanceApp.controller.dto.request.CategoryCreateRequest;
import group19.WebFinanceApp.controller.dto.request.CategoryUpdateRequest;
import group19.WebFinanceApp.controller.dto.response.CategoryResponse;
import group19.WebFinanceApp.model.Category;
import group19.WebFinanceApp.model.CategoryType;
import group19.WebFinanceApp.model.User;
import group19.WebFinanceApp.security.JwtUtil;
import group19.WebFinanceApp.repository.CategoryRepository;
import group19.WebFinanceApp.repository.TransactionRepository;
import group19.WebFinanceApp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryRepository categories;
    private final UserRepository users;
    private final TransactionRepository transactions;
    private final JwtUtil jwtUtil;

    @Autowired
    public CategoryController(CategoryRepository categories,
                              UserRepository users,
                              TransactionRepository transactions,
                              JwtUtil jwtUtil) {
        this.categories = categories;
        this.users = users;
        this.transactions = transactions;
        this.jwtUtil = jwtUtil;
    }

    // ====== LIST sa filterima ?ownerId=&type=&includeArchived=
    @GetMapping
    public List<CategoryResponse> list(@RequestParam(required = false) Long ownerId,
                                       @RequestParam(required = false) CategoryType type,
                                       @RequestParam(defaultValue = "false") boolean includeArchived) {
        // radi kompatibilnosti, filtriramo u memoriji
        return categories.findAll().stream()
                .filter(c -> ownerId == null || (c.getOwner() != null && c.getOwner().getId().equals(ownerId)))
                .filter(c -> type == null || c.getType() == type)
                .filter(c -> includeArchived || !c.isArchived())
                .map(this::toResponse)
                .toList();
    }

    // GLOBAL list (ako ti treba poseban endpoint)
    @GetMapping("/global")
    public List<CategoryResponse> global() {
        return categories.findByOwnerIsNull().stream().map(this::toResponse).toList();
    }

    // OWNER list (includeGlobal=true -> vrati i globalne)
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
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).<CategoryResponse>build());
    }

    // Helper: Get current user from JWT token in header
    private User getCurrentUser(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7).trim();
        Long userId = null;
        try {
            if (!jwtUtil.isValid(token)) return null;
            userId = jwtUtil.getUserId(token);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (userId == null) return null;
        return users.findById(userId).orElse(null);
    }

    // CREATE (ownerId je obavezan za user; ako je null -> global, ali samo admin može praviti globalne)
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryCreateRequest in,
                                                   Authentication authentication,
                                                   HttpServletRequest request) {
        User currentUser = getCurrentUser(request);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        boolean isAdmin = currentUser.getRole().equals("ADMIN");

        User owner = null;
        if (isAdmin) {
            // Admin može da pravi globalne ili lične
            if (in.getOwnerId() != null) {
                owner = users.findById(in.getOwnerId()).orElse(null);
                if (owner == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                }
            }
            // Ako ownerId == null, owner ostaje null (globalna)
        } else {
            // OwnerId mora biti postavljen za usera
            if (in.getOwnerId() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(null);
            }
            if (!in.getOwnerId().equals(currentUser.getId())) {
                // Ne dozvoli da user kreira za drugog usera
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            owner = currentUser;
        }

        // anti-duplikat po (owner, name, type)
        boolean duplicate = (owner == null)
                ? categories.existsByOwnerIsNullAndNameIgnoreCaseAndType(in.getName(), in.getType())
                : categories.existsByOwnerIdAndNameIgnoreCaseAndType(owner.getId(), in.getName(), in.getType());
        if (duplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Category c = new Category();
        c.setOwner(owner);
        c.setName(in.getName());
        c.setType(in.getType());
        c.setColor(in.getColor());
        c.setArchived(false);
        c.setPredefined(owner == null);

        c = categories.save(c);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(c));
    }

    // UPDATE: menja name/color/archived; TYPE NE MENJAMO
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CategoryUpdateRequest in) {
        return categories.findById(id)
                .map(c -> {
                    // zabrani izmenu predefinisanih/globalnih
                    if (c.isPredefined() || c.getOwner() == null) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).<CategoryResponse>build();
                    }

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
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).<CategoryResponse>build());
    }

    // PATCH /{id}/archive?archived=true|false
    @PatchMapping("/{id}/archive")
    public ResponseEntity<CategoryResponse> archive(@PathVariable Long id,
                                                    @RequestParam(defaultValue = "true") boolean archived) {
        return categories.findById(id)
                .map(c -> {
                    if (c.isPredefined() || c.getOwner() == null) {
                        return ResponseEntity.status(HttpStatus.CONFLICT).<CategoryResponse>build();
                    }
                    c.setArchived(archived);
                    categories.save(c);
                    return ResponseEntity.ok(toResponse(c));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).<CategoryResponse>build());
    }

    // DELETE – 409 ako je kategorija korišćena u transakcijama ili ako je predefinisana/globalna
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        var opt = categories.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var c = opt.get();

        // zabrani brisanje predefinisanih/globalnih
        if (c.isPredefined() || c.getOwner() == null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        // PROVERA: postoji li transakcija koja koristi ovu kategoriju?
        if (transactions.existsByCategoryId(c.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        categories.delete(c);
        return ResponseEntity.noContent().build();
    }

    // POST /{id}/use?ownerId=123  -> kreira "ličnu" kopiju globalne kategorije
    @PostMapping("/{id}/use")
    @Transactional
    public ResponseEntity<?> use(@PathVariable Long id, @RequestParam("ownerId") Long ownerId) {
        var global = categories.findById(id).orElse(null);
        if (global == null || global.getOwner() != null || !global.isPredefined()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Only predefined (global) categories can be used."));
        }

        var owner = users.findById(ownerId).orElse(null);
        if (owner == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        // duplikat po (ownerId, name, type)?
        boolean duplicate = categories.existsByOwnerIdAndNameIgnoreCaseAndType(
                ownerId, global.getName(), global.getType());
        if (duplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "You already have this category."));
        }

        var copy = new Category();
        copy.setName(global.getName());
        copy.setType(global.getType());
        copy.setColor(global.getColor());
        copy.setArchived(false);
        copy.setOwner(owner);
        copy.setPredefined(false);

        categories.save(copy);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(copy));
    }

    // mapper
    private CategoryResponse toResponse(Category c) {
        var dto = new CategoryResponse(
                c.getId(),
                c.getOwner() == null ? null : c.getOwner().getId(),
                c.getName(),
                c.getType(),
                c.getColor(),
                c.isArchived(),
                c.getCreatedAt()
        );
        dto.setPredefined(c.isPredefined());
        dto.setMine(c.getOwner() != null);
        return dto;
    }
}