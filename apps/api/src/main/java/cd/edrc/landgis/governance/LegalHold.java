package cd.edrc.landgis.governance;

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
@Table(schema = "governance", name = "legal_holds")
public class LegalHold {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String targetType;

    @Column(nullable = false)
    private UUID targetId;

    @Column(nullable = false)
    private String holdReason;

    @Column(nullable = false)
    private String authorityReference;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LegalHoldStatus status;

    @Column(nullable = false)
    private UUID placedByUserId;

    @Column(nullable = false)
    private String placedBy;

    @Column(nullable = false)
    private OffsetDateTime placedAt;

    private UUID releasedByUserId;
    private String releasedBy;
    private OffsetDateTime releasedAt;
    private String releaseReason;

    @Version
    private long version;

    protected LegalHold() {
    }

    public LegalHold(
            UUID id,
            String targetType,
            UUID targetId,
            String holdReason,
            String authorityReference,
            UUID placedByUserId,
            String placedBy) {
        this.id = id;
        this.targetType = targetType;
        this.targetId = targetId;
        this.holdReason = holdReason;
        this.authorityReference = authorityReference;
        this.status = LegalHoldStatus.ACTIVE;
        this.placedByUserId = placedByUserId;
        this.placedBy = placedBy;
        this.placedAt = OffsetDateTime.now();
    }

    public void release(UUID releasedByUserId, String releasedBy, String releaseReason) {
        this.status = LegalHoldStatus.RELEASED;
        this.releasedByUserId = releasedByUserId;
        this.releasedBy = releasedBy;
        this.releasedAt = OffsetDateTime.now();
        this.releaseReason = releaseReason;
    }

    public UUID id() {
        return id;
    }

    public String targetType() {
        return targetType;
    }

    public UUID targetId() {
        return targetId;
    }

    public String holdReason() {
        return holdReason;
    }

    public String authorityReference() {
        return authorityReference;
    }

    public LegalHoldStatus status() {
        return status;
    }
}
