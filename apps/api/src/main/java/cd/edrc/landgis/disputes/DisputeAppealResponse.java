package cd.edrc.landgis.disputes;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DisputeAppealResponse(
        UUID id,
        UUID caseId,
        String appealType,
        String grounds,
        String status,
        UUID filedByUserId,
        String filedBy,
        OffsetDateTime filedAt) {
}
