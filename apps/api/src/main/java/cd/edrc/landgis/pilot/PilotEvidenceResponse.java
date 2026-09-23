package cd.edrc.landgis.pilot;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PilotEvidenceResponse(
        UUID id,
        UUID pilotId,
        String evidenceType,
        String referenceType,
        UUID referenceId,
        String externalReference,
        String summary,
        String addedBy,
        OffsetDateTime addedAt) {
    static PilotEvidenceResponse from(PilotEvidence evidence) {
        return new PilotEvidenceResponse(
                evidence.id(),
                evidence.pilotId(),
                evidence.evidenceType().name(),
                evidence.referenceType(),
                evidence.referenceId(),
                evidence.externalReference(),
                evidence.summary(),
                evidence.addedBy(),
                evidence.addedAt());
    }
}
