package cd.edrc.landgis.audit;

import java.util.UUID;

public record RegisteredDeviceLifecycleResponse(
        RegisteredDeviceResponse device,
        String requestedAction,
        String workflowStatus,
        UUID taskId) {
}
