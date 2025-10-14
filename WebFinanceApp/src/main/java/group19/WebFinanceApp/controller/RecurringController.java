package group19.WebFinanceApp.controller;

import group19.WebFinanceApp.controller.dto.request.RecurringTemplateCreateRequest;
import group19.WebFinanceApp.controller.dto.request.RecurringTemplateUpdateRequest;
import group19.WebFinanceApp.controller.dto.response.RecurringTemplateResponse;
import group19.WebFinanceApp.controller.dto.response.TransactionResponse;
import group19.WebFinanceApp.model.RecurringTemplate;
import group19.WebFinanceApp.model.Transaction;
import group19.WebFinanceApp.repository.RecurringTemplateRepository;
import group19.WebFinanceApp.service.RecurringService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/recurrings")
public class RecurringController {

    private final RecurringService service;
    private final RecurringTemplateRepository templates;

    public RecurringController(RecurringService service, RecurringTemplateRepository templates) {
        this.service = service;
        this.templates = templates;
    }

    /* ===== CRUD ===== */

    @PostMapping
    public ResponseEntity<RecurringTemplateResponse> create(@Valid @RequestBody RecurringTemplateCreateRequest in) {
        var t = service.create(in);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(t));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecurringTemplateResponse> update(@PathVariable Long id,
                                                            @Valid @RequestBody RecurringTemplateUpdateRequest in) {
        return service.update(id, in)
                .map(x -> ResponseEntity.ok(toResponse(x)))
                .orElseGet(() -> ResponseEntity.notFound().<RecurringTemplateResponse>build());
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<RecurringTemplateResponse> toggle(@PathVariable Long id,
                                                            @RequestParam boolean active) {
        return service.toggle(id, active)
                .map(x -> ResponseEntity.ok(toResponse(x)))
                .orElseGet(() -> ResponseEntity.notFound().<RecurringTemplateResponse>build());
    }

    @GetMapping
    public List<RecurringTemplateResponse> list(@RequestParam Long ownerId) {
        return service.listByOwner(ownerId).stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecurringTemplateResponse> byId(@PathVariable Long id) {
        return templates.findById(id)
                .map(t -> ResponseEntity.ok(toResponse(t)))
                .orElseGet(() -> ResponseEntity.notFound().<RecurringTemplateResponse>build());
    }

    /* ===== Preview & Run ===== */

    @GetMapping("/{id}/preview")
    public ResponseEntity<List<LocalDate>> preview(@PathVariable Long id,
                                                   @RequestParam(required = false) String from,
                                                   @RequestParam(required = false) String to) {
        LocalDate f = (from == null || from.isBlank()) ? null : LocalDate.parse(from);
        LocalDate tt = (to == null || to.isBlank()) ? null : LocalDate.parse(to);
        var dates = service.preview(id, f, tt);
        return ResponseEntity.ok(dates);
    }

    @PostMapping("/{id}/run-now")
    public ResponseEntity<TransactionResponse> runNow(@PathVariable Long id) {
        return service.runNow(id)
                .map(tx -> ResponseEntity.status(HttpStatus.CREATED).body(toTxResponse(tx)))
                .orElseGet(() -> ResponseEntity.notFound().<TransactionResponse>build());
    }

    @PostMapping("/run-due")
    public ResponseEntity<List<TransactionResponse>> runDue() {
        List<Transaction> list = service.runDue();
        var body = list.stream().map(this::toTxResponse).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    /* ===== mappers ===== */

    private RecurringTemplateResponse toResponse(RecurringTemplate t) {
        return new RecurringTemplateResponse(
                t.getId(),
                t.getOwner().getId(),
                t.getWallet().getId(),
                t.getCategory().getId(),
                t.getWallet().getCurrency().getCode(),
                t.getName(),
                t.getAmount(),
                t.getDescriptionTemplate(),
                t.getFrequency(),
                t.getInterval(),
                t.getStartDate(),
                t.getEndDate(),
                t.getNextRunDate(),
                t.isActive(),
                t.getCreatedAt(),
                t.getUpdatedAt()
        );
    }

    private TransactionResponse toTxResponse(group19.WebFinanceApp.model.Transaction tx) {
        var c = tx.getCategory();
        return new TransactionResponse(
                tx.getId(),
                tx.getWallet().getId(),
                c.getId(),
                c.getName(),
                c.getType(),
                tx.getAmount(),
                tx.getDescription(),
                tx.getOccurredAt(),
                tx.getTransferId()
        );
    }
}