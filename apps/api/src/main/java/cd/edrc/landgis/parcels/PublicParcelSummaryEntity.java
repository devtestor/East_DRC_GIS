package cd.edrc.landgis.parcels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "parcels", name = "public_parcel_summaries")
public class PublicParcelSummaryEntity {
    @Id
    private UUID parcelId;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String proposedUpi;

    @Column(nullable = false)
    private UUID administrativeUnitId;

    @Column(nullable = false)
    private String administrativeUnitName;

    @Column(nullable = false)
    private String administrativeUnitType;

    private String landUse;
    private String tenureClassification;

    @Column(nullable = false)
    private OffsetDateTime publishedAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    protected PublicParcelSummaryEntity() {
    }

    public PublicParcelSummaryEntity(
            UUID parcelId,
            String status,
            String proposedUpi,
            UUID administrativeUnitId,
            String administrativeUnitName,
            String administrativeUnitType,
            String landUse,
            String tenureClassification) {
        this.parcelId = parcelId;
        this.status = status;
        this.proposedUpi = proposedUpi;
        this.administrativeUnitId = administrativeUnitId;
        this.administrativeUnitName = administrativeUnitName;
        this.administrativeUnitType = administrativeUnitType;
        this.landUse = landUse;
        this.tenureClassification = tenureClassification;
        this.publishedAt = OffsetDateTime.now();
        this.updatedAt = this.publishedAt;
    }

    public PublicParcelSummary toSummary() {
        return new PublicParcelSummary(
                parcelId,
                status,
                proposedUpi,
                administrativeUnitId,
                administrativeUnitName,
                administrativeUnitType,
                landUse,
                tenureClassification);
    }
}
