package group19.WebFinanceApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table (name = "currencies")
public class Currency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ISO kod valute (npr. RSD, EUR). Ne menjamo ga nakon kreiranja.
    @NotBlank
    @Pattern(regexp = "^[A-Z]{3}$", message = "Code must be exactly 3 uppercase letters")
    @Column(nullable = false, length = 3, updatable = false)
    private String code;

    @NotBlank
    @Column(nullable = false, length = 64)
    private String name;

    // kurs u odnosu na EUR (snapshot referenca)
    @NotNull
    @DecimalMin(value = "0.000001")
    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal valueVsEur;

    @Column(nullable = false)
    private Instant updatedAt;

    public Currency() {}

    public Currency(String code, String name, BigDecimal valueVsEur) {
        this.code = code;
        this.name = name;
        this.valueVsEur = valueVsEur;
    }

    @PrePersist @PreUpdate
    protected void touchUpdated() {
        this.updatedAt = Instant.now();
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getValueVsEur() { return valueVsEur; }
    public void setValueVsEur(BigDecimal valueVsEur) { this.valueVsEur = valueVsEur; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
