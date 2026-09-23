package cd.edrc.landgis.administration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "administration", name = "administrative_units")
public class AdministrativeUnit {
    @Id
    private UUID id;

    private UUID parentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdministrativeUnitType unitType;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdministrativeUnitStatus status;

    @Column(nullable = false)
    private LocalDate validFrom;

    private LocalDate validTo;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private long version;

    protected AdministrativeUnit() {
    }

    public AdministrativeUnit(UUID id, UUID parentId, AdministrativeUnitType unitType, String code, String name, LocalDate validFrom) {
        this.id = id;
        this.parentId = parentId;
        this.unitType = unitType;
        this.code = code;
        this.name = name;
        this.status = AdministrativeUnitStatus.ACTIVE;
        this.validFrom = validFrom;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public UUID id() {
        return id;
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }

    public AdministrativeUnitType unitType() {
        return unitType;
    }
}
