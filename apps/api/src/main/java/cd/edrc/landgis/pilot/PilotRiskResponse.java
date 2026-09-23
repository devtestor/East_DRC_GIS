package cd.edrc.landgis.pilot;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PilotRiskResponse(
        UUID id,
        UUID pilotId,
        String severity,
        String status,
        String title,
        String mitigationPlan,
        boolean blockingGoLive,
        String createdBy,
        OffsetDateTime createdAt) {
    static PilotRiskResponse from(PilotRisk risk) {
        return new PilotRiskResponse(
                risk.id(),
                risk.pilotId(),
                risk.severity().name(),
                risk.status().name(),
                risk.title(),
                risk.mitigationPlan(),
                risk.blockingGoLive(),
                risk.createdBy(),
                risk.createdAt());
    }
}
