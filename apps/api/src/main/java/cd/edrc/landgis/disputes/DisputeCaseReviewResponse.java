package cd.edrc.landgis.disputes;

import java.util.UUID;

public record DisputeCaseReviewResponse(
        DisputeCaseResponse disputeCase,
        String requestedAction,
        String workflowStatus,
        UUID taskId) {
}
