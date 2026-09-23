package cd.edrc.landgis.parcels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "parcels", name = "parcel_identifiers")
public class ParcelIdentifier {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID parcelId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdentifierType identifierType;

    @Column(nullable = false)
    private String identifierValue;

    private UUID upiSchemeVersionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IdentifierStatus status;

    @Column(nullable = false)
    private OffsetDateTime assignedAt;

    protected ParcelIdentifier() {
    }

    public ParcelIdentifier(UUID id, UUID parcelId, String upi, UUID upiSchemeVersionId) {
        this.id = id;
        this.parcelId = parcelId;
        this.identifierType = IdentifierType.PROPOSED_UPI;
        this.identifierValue = upi;
        this.upiSchemeVersionId = upiSchemeVersionId;
        this.status = IdentifierStatus.ACTIVE;
        this.assignedAt = OffsetDateTime.now();
    }

    public String identifierValue() {
        return identifierValue;
    }

    public UUID parcelId() {
        return parcelId;
    }
}
