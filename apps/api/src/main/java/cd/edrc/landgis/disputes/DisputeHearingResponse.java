package cd.edrc.landgis.disputes;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DisputeHearingResponse(
        UUID id,
        UUID caseId,
        OffsetDateTime scheduledFor,
        String venue,
        String notes,
        String status,
        UUID scheduledByUserId,
        String scheduledBy,
        OffsetDateTime scheduledAt,
        OffsetDateTime heldAt) {
}
