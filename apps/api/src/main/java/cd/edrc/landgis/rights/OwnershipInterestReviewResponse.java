package cd.edrc.landgis.rights;

import java.util.UUID;

public record OwnershipInterestReviewResponse(
        OwnershipInterestResponse interest,
        String requestedAction,
        String workflowStatus,
        UUID taskId) {
}
