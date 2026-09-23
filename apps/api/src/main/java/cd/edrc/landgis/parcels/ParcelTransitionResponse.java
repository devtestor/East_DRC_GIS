package cd.edrc.landgis.parcels;

import java.util.UUID;

record ParcelTransitionResponse(
        UUID parcelId,
        String currentStatus,
        String requestedStatus,
        String outcome,
        UUID workflowTaskId) {
}
