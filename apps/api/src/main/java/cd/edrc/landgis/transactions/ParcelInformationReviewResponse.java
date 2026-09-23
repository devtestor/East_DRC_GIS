package cd.edrc.landgis.transactions;

import java.util.UUID;

public record ParcelInformationReviewResponse(
        ParcelInformationApplicationResponse application,
        String requestedAction,
        String workflowStatus,
        UUID taskId) {
}
