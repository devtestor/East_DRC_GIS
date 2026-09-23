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
@Table(schema = "pilot", name = "pilot_signoffs")
public class PilotSignoff {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID pilotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PilotSignoffType signoffType;

    @Column(nullable = false)
    private String requiredRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PilotSignoffStatus status;

    @Column(nullable = false)
    private String summary;

    private UUID workflowTaskId;
    private UUID decidedByUserId;
    private String decidedBy;
    private OffsetDateTime decidedAt;

    @Column(nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private long version;

    protected PilotSignoff() {
    }

    public PilotSignoff(
            UUID id,
            UUID pilotId,
            PilotSignoffType signoffType,
            String requiredRole,
            String summary,
            UUID createdByUserId,
            String createdBy) {
        this.id = id;
        this.pilotId = pilotId;
        this.signoffType = signoffType;
        this.requiredRole = requiredRole;
        this.summary = summary;
        this.createdByUserId = createdByUserId;
        this.createdBy = createdBy;
        this.status = PilotSignoffStatus.PENDING;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public UUID id() { return id; }
    public UUID pilotId() { return pilotId; }
    public PilotSignoffType signoffType() { return signoffType; }
    public String requiredRole() { return requiredRole; }
    public PilotSignoffStatus status() { return status; }
    public String summary() { return summary; }
    public UUID workflowTaskId() { return workflowTaskId; }
    public UUID decidedByUserId() { return decidedByUserId; }
    public String decidedBy() { return decidedBy; }
    public OffsetDateTime decidedAt() { return decidedAt; }
    public UUID createdByUserId() { return createdByUserId; }
    public String createdBy() { return createdBy; }
    public OffsetDateTime createdAt() { return createdAt; }

    public void attachTask(UUID workflowTaskId) {
        this.workflowTaskId = workflowTaskId;
        this.updatedAt = OffsetDateTime.now();
    }

    public void approve(UUID actorUserId, String actor) {
        this.status = PilotSignoffStatus.APPROVED;
        this.decidedByUserId = actorUserId;
        this.decidedBy = actor;
        this.decidedAt = OffsetDateTime.now();
        this.updatedAt = this.decidedAt;
    }

    public void reject(UUID actorUserId, String actor) {
        this.status = PilotSignoffStatus.REJECTED;
        this.decidedByUserId = actorUserId;
        this.decidedBy = actor;
        this.decidedAt = OffsetDateTime.now();
        this.updatedAt = this.decidedAt;
    }
}
