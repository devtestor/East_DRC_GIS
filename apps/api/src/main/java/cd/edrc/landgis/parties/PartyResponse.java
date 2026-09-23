package cd.edrc.landgis.parties;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PartyResponse(
        UUID id,
        String partyType,
        String displayName,
        String dataConfidence,
        String verificationStatus,
        UUID createdByUserId,
        String createdBy,
        OffsetDateTime createdAt) {
}
