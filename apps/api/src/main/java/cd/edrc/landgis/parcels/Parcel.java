package cd.edrc.landgis.parcels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "parcels", name = "parcels")
public class Parcel {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ParcelStatus status;

    @Column(nullable = false)
    private UUID administrativeUnitId;

    private String landUse;
    private String zoningClassification;
    private String tenureClassification;

    @Column(nullable = false)
    private String dataQualityStatus;

    @Column(nullable = false)
    private String sourceSystem;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Version
    private long version;

    protected Parcel() {
    }

    public Parcel(UUID id, UUID administrativeUnitId, String landUse, String tenureClassification) {
        this.id = id;
        this.status = ParcelStatus.DRAFT;
        this.administrativeUnitId = administrativeUnitId;
        this.landUse = landUse;
        this.tenureClassification = tenureClassification;
        this.dataQualityStatus = "UNVERIFIED";
        this.sourceSystem = "edrc-land-gis";
        this.createdAt = OffsetDateTime.now();
    }

    public UUID id() {
        return id;
    }

    public ParcelStatus status() {
        return status;
    }

    public UUID administrativeUnitId() {
        return administrativeUnitId;
    }

    public String landUse() {
        return landUse;
    }

    public String tenureClassification() {
        return tenureClassification;
    }

    public void transitionTo(ParcelStatus nextStatus) {
        this.status = nextStatus;
    }
}
