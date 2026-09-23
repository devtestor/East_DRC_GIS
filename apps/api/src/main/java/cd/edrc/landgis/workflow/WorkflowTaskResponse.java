package cd.edrc.landgis.workflow;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkflowTaskResponse(
        UUID id,
        String workflowType,
        String targetType,
        UUID targetId,
        String requestedAction,
        UUID requestedByUserId,
        String requestedBy,
        String status,
        String priority,
        OffsetDateTime createdAt,
        String assignedToActor,
        UUID assignedToUserId,
        String assignedToRole,
        OffsetDateTime claimedAt,
        String decisionReason,
        OffsetDateTime decidedAt,
        UUID decidedByUserId,
        String decidedBy) {
    public static WorkflowTaskResponse from(WorkflowTask task) {
        return new WorkflowTaskResponse(
                task.id(),
                task.workflowType(),
                task.targetType(),
                task.targetId(),
                task.requestedAction(),
                task.requestedByUserId(),
                task.requestedBy(),
                task.status().name(),
                task.priority().name(),
                task.createdAt(),
                task.assignedToActor(),
                task.assignedToUserId(),
                task.assignedToRole(),
                task.claimedAt(),
                task.decisionReason(),
                task.decidedAt(),
                task.decidedByUserId(),
                task.decidedBy());
    }
}
