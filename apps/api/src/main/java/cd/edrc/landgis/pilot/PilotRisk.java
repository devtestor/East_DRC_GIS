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
@Table(schema = "pilot", name = "pilot_risks")
public class PilotRisk {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID pilotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PilotRiskSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PilotRiskStatus status;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String mitigationPlan;

    @Column(nullable = false)
    private boolean blockingGoLive;

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

    protected PilotRisk() {
    }

    public PilotRisk(
            UUID id,
            UUID pilotId,
            PilotRiskSeverity severity,
            String title,
            String mitigationPlan,
            boolean blockingGoLive,
            UUID createdByUserId,
            String createdBy) {
        this.id = id;
        this.pilotId = pilotId;
        this.severity = severity;
        this.title = title;
        this.mitigationPlan = mitigationPlan;
        this.blockingGoLive = blockingGoLive;
        this.createdByUserId = createdByUserId;
        this.createdBy = createdBy;
        this.status = PilotRiskStatus.OPEN;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public UUID id() { return id; }
    public UUID pilotId() { return pilotId; }
    public PilotRiskSeverity severity() { return severity; }
    public PilotRiskStatus status() { return status; }
    public String title() { return title; }
    public String mitigationPlan() { return mitigationPlan; }
    public boolean blockingGoLive() { return blockingGoLive; }
    public UUID createdByUserId() { return createdByUserId; }
    public String createdBy() { return createdBy; }
    public OffsetDateTime createdAt() { return createdAt; }

    public void updateStatus(PilotRiskStatus status, boolean blockingGoLive) {
        this.status = status;
        this.blockingGoLive = blockingGoLive;
        this.updatedAt = OffsetDateTime.now();
    }
}
