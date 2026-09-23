package cd.edrc.landgis.pilot;

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
@Table(schema = "pilot", name = "pilot_operational_gates")
public class PilotOperationalGate {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID pilotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PilotOperationalGateType gateType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PilotOperationalGateStatus status;

    @Column(nullable = false)
    private String ownerRole;

    @Column(nullable = false)
    private String summary;

    private String evidenceReference;

    @Column(nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false)
    private String createdBy;

    private UUID decidedByUserId;
    private String decidedBy;
    private OffsetDateTime decidedAt;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private long version;

    protected PilotOperationalGate() {
    }

    public PilotOperationalGate(
            UUID id,
            UUID pilotId,
            PilotOperationalGateType gateType,
            String ownerRole,
            String summary,
            String evidenceReference,
            UUID createdByUserId,
            String createdBy) {
        this.id = id;
        this.pilotId = pilotId;
        this.gateType = gateType;
        this.ownerRole = ownerRole;
        this.summary = summary;
        this.evidenceReference = evidenceReference;
        this.createdByUserId = createdByUserId;
        this.createdBy = createdBy;
        this.status = PilotOperationalGateStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public UUID id() { return id; }
    public UUID pilotId() { return pilotId; }
    public PilotOperationalGateType gateType() { return gateType; }
    public PilotOperationalGateStatus status() { return status; }
    public String ownerRole() { return ownerRole; }
    public String summary() { return summary; }
    public String evidenceReference() { return evidenceReference; }
    public String createdBy() { return createdBy; }
    public OffsetDateTime createdAt() { return createdAt; }
    public String decidedBy() { return decidedBy; }
    public OffsetDateTime decidedAt() { return decidedAt; }

    public void decide(PilotOperationalGateStatus status, String evidenceReference, UUID actorUserId, String actor) {
        this.status = status;
        this.evidenceReference = evidenceReference == null || evidenceReference.isBlank()
                ? this.evidenceReference
                : evidenceReference;
        this.decidedByUserId = actorUserId;
        this.decidedBy = actor;
        this.decidedAt = OffsetDateTime.now();
        this.updatedAt = this.decidedAt;
    }
}
