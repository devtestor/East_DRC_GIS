package cd.edrc.landgis.pilot;

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
@Table(schema = "pilot", name = "pilot_readiness_records")
public class PilotReadinessRecord {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String geographyScope;

    @Column(nullable = false)
    private String pilotOwner;

    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PilotStatus status;

    private String finalDecisionReason;
    private UUID finalDecisionTaskId;

    @Column(nullable = false)
    private UUID requestedByUserId;

    @Column(nullable = false)
    private String requestedBy;

    private UUID decidedByUserId;
    private String decidedBy;
    private OffsetDateTime decidedAt;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private long version;

    protected PilotReadinessRecord() {
    }

    public PilotReadinessRecord(
            UUID id,
            String title,
            String geographyScope,
            String pilotOwner,
            LocalDate plannedStartDate,
            LocalDate plannedEndDate,
            UUID requestedByUserId,
            String requestedBy) {
        this.id = id;
        this.title = title;
        this.geographyScope = geographyScope;
        this.pilotOwner = pilotOwner;
        this.plannedStartDate = plannedStartDate;
        this.plannedEndDate = plannedEndDate;
        this.requestedByUserId = requestedByUserId;
        this.requestedBy = requestedBy;
        this.status = PilotStatus.DRAFT;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public UUID id() { return id; }
    public String title() { return title; }
    public String geographyScope() { return geographyScope; }
    public String pilotOwner() { return pilotOwner; }
    public LocalDate plannedStartDate() { return plannedStartDate; }
    public LocalDate plannedEndDate() { return plannedEndDate; }
    public PilotStatus status() { return status; }
    public String finalDecisionReason() { return finalDecisionReason; }
    public UUID finalDecisionTaskId() { return finalDecisionTaskId; }
    public UUID requestedByUserId() { return requestedByUserId; }
    public String requestedBy() { return requestedBy; }
    public UUID decidedByUserId() { return decidedByUserId; }
    public String decidedBy() { return decidedBy; }
    public OffsetDateTime decidedAt() { return decidedAt; }
    public OffsetDateTime createdAt() { return createdAt; }
    public OffsetDateTime updatedAt() { return updatedAt; }

    public void markUnderReview(UUID taskId) {
        this.status = PilotStatus.UNDER_REVIEW;
        this.finalDecisionTaskId = taskId;
        this.updatedAt = OffsetDateTime.now();
    }

    public void approveGo(String reason, UUID decidedByUserId, String decidedBy) {
        this.status = PilotStatus.GO_APPROVED;
        this.finalDecisionReason = reason;
        this.decidedByUserId = decidedByUserId;
        this.decidedBy = decidedBy;
        this.decidedAt = OffsetDateTime.now();
        this.updatedAt = this.decidedAt;
    }

    public void rejectGo(String reason, UUID decidedByUserId, String decidedBy) {
        this.status = PilotStatus.NO_GO;
        this.finalDecisionReason = reason;
        this.decidedByUserId = decidedByUserId;
        this.decidedBy = decidedBy;
        this.decidedAt = OffsetDateTime.now();
        this.updatedAt = this.decidedAt;
    }
}
