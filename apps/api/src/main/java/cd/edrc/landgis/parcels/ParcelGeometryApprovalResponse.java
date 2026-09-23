package cd.edrc.landgis.parcels;

import java.util.UUID;

public record ParcelGeometryApprovalResponse(
        ParcelGeometryVersionResponse geometry,
        String requestedAction,
        String workflowStatus,
        UUID taskId) {
}
