package cd.edrc.landgis.surveys;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SurveyResponse(
        UUID id,
        UUID parcelId,
        UUID assignedToUserId,
        String status,
        String purpose,
        int observationCount,
        OffsetDateTime submittedAt,
        OffsetDateTime createdAt) {
}
