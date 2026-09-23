package cd.edrc.landgis.pilot;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PilotSignoffResponse(
        UUID id,
        UUID pilotId,
        String signoffType,
        String requiredRole,
        String status,
        String summary,
        UUID workflowTaskId,
        String decidedBy,
        OffsetDateTime decidedAt,
        String createdBy,
        OffsetDateTime createdAt) {
    static PilotSignoffResponse from(PilotSignoff signoff) {
        return new PilotSignoffResponse(
                signoff.id(),
                signoff.pilotId(),
                signoff.signoffType().name(),
                signoff.requiredRole(),
                signoff.status().name(),
                signoff.summary(),
                signoff.workflowTaskId(),
                signoff.decidedBy(),
                signoff.decidedAt(),
                signoff.createdBy(),
                signoff.createdAt());
    }
}
