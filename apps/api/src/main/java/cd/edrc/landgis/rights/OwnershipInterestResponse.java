package cd.edrc.landgis.rights;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record OwnershipInterestResponse(
        UUID id,
        UUID parcelId,
        UUID partyId,
        String partyDisplayName,
        String interestType,
        String interestStatus,
        BigDecimal sharePercent,
        String dataConfidence,
        String source,
        OffsetDateTime effectiveFrom,
        OffsetDateTime effectiveTo,
        UUID createdByUserId,
        String createdBy,
        OffsetDateTime createdAt) {
}
