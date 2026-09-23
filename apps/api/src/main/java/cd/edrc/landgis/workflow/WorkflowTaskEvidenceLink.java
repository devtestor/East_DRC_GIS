package cd.edrc.landgis.workflow;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "workflow", name = "task_evidence_links")
public class WorkflowTaskEvidenceLink {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID taskId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowEvidenceType evidenceType;

    @Column(nullable = false)
    private String referenceType;

    private UUID referenceId;
    private String externalReference;

    @Column(nullable = false)
    private String summary;

    @Column(nullable = false)
    private UUID addedByUserId;

    @Column(nullable = false)
    private String addedBy;

    @Column(nullable = false)
    private OffsetDateTime addedAt;

    protected WorkflowTaskEvidenceLink() {
    }

    public WorkflowTaskEvidenceLink(
            UUID id,
            UUID taskId,
            WorkflowEvidenceType evidenceType,
            String referenceType,
            UUID referenceId,
            String externalReference,
            String summary,
            UUID addedByUserId,
            String addedBy) {
        this.id = id;
        this.taskId = taskId;
        this.evidenceType = evidenceType;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.externalReference = externalReference;
        this.summary = summary;
        this.addedByUserId = addedByUserId;
        this.addedBy = addedBy;
        this.addedAt = OffsetDateTime.now();
    }

    public UUID id() {
        return id;
    }

    public UUID taskId() {
        return taskId;
    }

    public WorkflowEvidenceType evidenceType() {
        return evidenceType;
    }

    public String referenceType() {
        return referenceType;
    }

    public UUID referenceId() {
        return referenceId;
    }

    public String externalReference() {
        return externalReference;
    }

    public String summary() {
        return summary;
    }

    public UUID addedByUserId() {
        return addedByUserId;
    }

    public String addedBy() {
        return addedBy;
    }

    public OffsetDateTime addedAt() {
        return addedAt;
    }
}
