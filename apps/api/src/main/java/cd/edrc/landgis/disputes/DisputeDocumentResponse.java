package cd.edrc.landgis.disputes;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DisputeDocumentResponse(
        UUID id,
        UUID caseId,
        UUID documentId,
        String relationship,
        String summary,
        UUID linkedByUserId,
        String linkedBy,
        OffsetDateTime linkedAt) {
}
