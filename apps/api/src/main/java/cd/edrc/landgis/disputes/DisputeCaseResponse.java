package cd.edrc.landgis.disputes;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DisputeCaseResponse(
        UUID id,
        UUID parcelId,
        String caseType,
        String status,
        String summary,
        String source,
        String authorityReference,
        UUID openedByUserId,
        String openedBy,
        OffsetDateTime openedAt,
        OffsetDateTime updatedAt) {
}
