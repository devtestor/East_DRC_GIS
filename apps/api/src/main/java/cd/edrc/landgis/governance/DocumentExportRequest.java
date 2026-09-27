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
@Table(schema = "governance", name = "export_requests")
public class DocumentExportRequest {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private String purpose;

    @Column(nullable = false)
    private boolean redactionPlanned;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExportRequestStatus status;

    @Column(nullable = false)
    private boolean redactionRequired;

    @Column(nullable = false)
    private boolean approvalRequired;

    @Column(nullable = false)
    private String blockerSummary;

    private UUID workflowTaskId;

    @Column(nullable = false)
    private UUID requestedByUserId;

    @Column(nullable = false)
    private String requestedBy;

    @Column(nullable = false)
    private OffsetDateTime requestedAt;

    private UUID decidedByUserId;
    private String decidedBy;
    private OffsetDateTime decidedAt;
    private String decisionReason;

    @Version
    private long version;

    protected DocumentExportRequest() {
    }

    public DocumentExportRequest(
            UUID id,
            UUID documentId,
            String purpose,
            boolean redactionPlanned,
            ExportRequestStatus status,
            boolean redactionRequired,
            boolean approvalRequired,
            String blockerSummary,
            UUID workflowTaskId,
            UUID requestedByUserId,
            String requestedBy) {
        this.id = id;
        this.documentId = documentId;
        this.purpose = purpose;
        this.redactionPlanned = redactionPlanned;
        this.status = status;
        this.redactionRequired = redactionRequired;
        this.approvalRequired = approvalRequired;
        this.blockerSummary = blockerSummary == null ? "" : blockerSummary;
        this.workflowTaskId = workflowTaskId;
        this.requestedByUserId = requestedByUserId;
        this.requestedBy = requestedBy;
        this.requestedAt = OffsetDateTime.now();
    }

    public void approve(UUID decidedByUserId, String decidedBy, String decisionReason) {
        this.status = ExportRequestStatus.APPROVED;
        this.decidedByUserId = decidedByUserId;
        this.decidedBy = decidedBy;
        this.decidedAt = OffsetDateTime.now();
        this.decisionReason = decisionReason;
    }

    public void assignWorkflowTask(UUID workflowTaskId) {
        this.workflowTaskId = workflowTaskId;
    }

    public void reject(UUID decidedByUserId, String decidedBy, String decisionReason) {
        this.status = ExportRequestStatus.REJECTED;
        this.decidedByUserId = decidedByUserId;
        this.decidedBy = decidedBy;
        this.decidedAt = OffsetDateTime.now();
        this.decisionReason = decisionReason;
    }

    public UUID id() {
        return id;
    }

    public UUID documentId() {
        return documentId;
    }

    public String purpose() {
        return purpose;
    }

    public boolean redactionPlanned() {
        return redactionPlanned;
    }

    public ExportRequestStatus status() {
        return status;
    }

    public boolean redactionRequired() {
        return redactionRequired;
    }

    public boolean approvalRequired() {
        return approvalRequired;
    }

    public String blockerSummary() {
        return blockerSummary;
    }

    public UUID workflowTaskId() {
        return workflowTaskId;
    }

    public UUID requestedByUserId() {
        return requestedByUserId;
    }

    public String requestedBy() {
        return requestedBy;
    }

    public OffsetDateTime requestedAt() {
        return requestedAt;
    }

    public UUID decidedByUserId() {
        return decidedByUserId;
    }

    public String decidedBy() {
        return decidedBy;
    }

    public OffsetDateTime decidedAt() {
        return decidedAt;
    }

    public String decisionReason() {
        return decisionReason;
    }
}
