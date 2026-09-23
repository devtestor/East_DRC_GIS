package cd.edrc.landgis.workflow;

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
@Table(schema = "workflow", name = "tasks")
public class WorkflowTask {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String workflowType;

    @Column(nullable = false)
    private String targetType;

    @Column(nullable = false)
    private UUID targetId;

    @Column(nullable = false)
    private String requestedAction;

    @Column(nullable = false)
    private String requestedBy;

    @Column(nullable = false)
    private UUID requestedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowTaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkflowTaskPriority priority;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime dueAt;
    private UUID assignedToUserId;
    private String assignedToActor;
    private String assignedToRole;
    private OffsetDateTime claimedAt;
    private String decisionReason;
    private OffsetDateTime decidedAt;
    private String decidedBy;
    private UUID decidedByUserId;

    @Version
    private long version;

    protected WorkflowTask() {
    }

    public WorkflowTask(
            UUID id,
            String workflowType,
            String targetType,
            UUID targetId,
            String requestedAction,
            UUID requestedByUserId,
            String requestedBy,
            String assignedToRole) {
        this.id = id;
        this.workflowType = workflowType;
        this.targetType = targetType;
        this.targetId = targetId;
        this.requestedAction = requestedAction;
        this.requestedByUserId = requestedByUserId;
        this.requestedBy = requestedBy;
        this.status = WorkflowTaskStatus.OPEN;
        this.priority = WorkflowTaskPriority.NORMAL;
        this.assignedToRole = assignedToRole;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID id() {
        return id;
    }

    public String workflowType() {
        return workflowType;
    }

    public String targetType() {
        return targetType;
    }

    public UUID targetId() {
        return targetId;
    }

    public String requestedAction() {
        return requestedAction;
    }

    public String requestedBy() {
        return requestedBy;
    }

    public UUID requestedByUserId() {
        return requestedByUserId;
    }

    public WorkflowTaskStatus status() {
        return status;
    }

    public WorkflowTaskPriority priority() {
        return priority;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }

    public String assignedToRole() {
        return assignedToRole;
    }

    public String assignedToActor() {
        return assignedToActor;
    }

    public UUID assignedToUserId() {
        return assignedToUserId;
    }

    public OffsetDateTime claimedAt() {
        return claimedAt;
    }

    public String decisionReason() {
        return decisionReason;
    }

    public OffsetDateTime decidedAt() {
        return decidedAt;
    }

    public String decidedBy() {
        return decidedBy;
    }

    public UUID decidedByUserId() {
        return decidedByUserId;
    }

    public void approve(String reason, UUID decidedByUserId, String decidedBy) {
        validateApprovalBy(decidedByUserId, decidedBy);
        this.status = WorkflowTaskStatus.APPROVED;
        this.decisionReason = reason;
        this.decidedBy = decidedBy;
        this.decidedByUserId = decidedByUserId;
        this.decidedAt = OffsetDateTime.now();
    }

    public void reject(String reason, UUID decidedByUserId, String decidedBy) {
        requireClaimedBy(decidedByUserId, decidedBy);
        this.status = WorkflowTaskStatus.REJECTED;
        this.decisionReason = reason;
        this.decidedBy = decidedBy;
        this.decidedByUserId = decidedByUserId;
        this.decidedAt = OffsetDateTime.now();
    }

    public void claim(UUID actorUserId, String actor) {
        requireOpen();
        this.status = WorkflowTaskStatus.CLAIMED;
        this.assignedToUserId = actorUserId;
        this.assignedToActor = actor;
        this.claimedAt = OffsetDateTime.now();
    }

    public void validateApprovalBy(UUID actorUserId, String actor) {
        requireClaimedBy(actorUserId, actor);
        requireDifferentDecisionActor(actorUserId, actor);
    }

    private void requireOpen() {
        if (status != WorkflowTaskStatus.OPEN) {
            throw new WorkflowTaskNotOpenException(id, status);
        }
    }

    private void requireClaimedBy(UUID actorUserId, String actor) {
        if (status == WorkflowTaskStatus.OPEN || assignedToUserId == null) {
            throw new WorkflowTaskAssignmentRequiredException(id);
        }
        if (status != WorkflowTaskStatus.CLAIMED) {
            throw new WorkflowTaskNotOpenException(id, status);
        }
        if (!assignedToUserId.equals(actorUserId)) {
            throw new WorkflowTaskAssignedToAnotherActorException(id, assignedToActor, actor);
        }
    }

    private void requireDifferentDecisionActor(UUID decidedByUserId, String decidedBy) {
        if (requestedByUserId.equals(decidedByUserId)) {
            throw new MakerCheckerViolationException(id, decidedBy);
        }
    }
}
