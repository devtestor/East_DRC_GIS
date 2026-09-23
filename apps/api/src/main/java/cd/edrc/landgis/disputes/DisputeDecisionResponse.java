package cd.edrc.landgis.disputes;

import java.util.UUID;

public record DisputeDecisionResponse(
        DisputeCaseResponse disputeCase,
        String requestedAction,
        String workflowStatus,
        UUID taskId) {
}
