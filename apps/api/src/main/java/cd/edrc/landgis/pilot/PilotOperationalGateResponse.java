package cd.edrc.landgis.pilot;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PilotOperationalGateResponse(
        UUID id,
        UUID pilotId,
        String gateType,
        String status,
        String ownerRole,
        String summary,
        String evidenceReference,
        String createdBy,
        OffsetDateTime createdAt,
        String decidedBy,
        OffsetDateTime decidedAt) {
    static PilotOperationalGateResponse from(PilotOperationalGate gate) {
        return new PilotOperationalGateResponse(
                gate.id(),
                gate.pilotId(),
                gate.gateType().name(),
                gate.status().name(),
                gate.ownerRole(),
                gate.summary(),
                gate.evidenceReference(),
                gate.createdBy(),
                gate.createdAt(),
                gate.decidedBy(),
                gate.decidedAt());
    }
}
