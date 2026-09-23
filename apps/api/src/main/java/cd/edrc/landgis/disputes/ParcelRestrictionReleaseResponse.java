package cd.edrc.landgis.disputes;

import java.util.UUID;

public record ParcelRestrictionReleaseResponse(
        ParcelRestrictionResponse restriction,
        String requestedAction,
        String workflowStatus,
        UUID taskId) {
}
