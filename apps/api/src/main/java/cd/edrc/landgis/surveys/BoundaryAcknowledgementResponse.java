package cd.edrc.landgis.surveys;

import java.time.OffsetDateTime;
import java.util.UUID;

public record BoundaryAcknowledgementResponse(
        UUID id,
        UUID surveyId,
        String neighborName,
        String status,
        String note,
        OffsetDateTime acknowledgedAt) {
}
