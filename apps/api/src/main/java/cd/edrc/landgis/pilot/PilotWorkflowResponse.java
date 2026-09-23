package cd.edrc.landgis.pilot;

import java.util.UUID;

public record PilotWorkflowResponse(
        PilotReadinessResponse pilot,
        String requestedAction,
        String workflowStatus,
        UUID taskId) {
}
