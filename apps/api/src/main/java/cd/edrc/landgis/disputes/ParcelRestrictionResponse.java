package cd.edrc.landgis.disputes;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ParcelRestrictionResponse(
        UUID id,
        UUID parcelId,
        String restrictionType,
        String status,
        String source,
        String authorityReference,
        String summary,
        boolean blocksOwnershipChanges,
        OffsetDateTime effectiveFrom,
        OffsetDateTime effectiveTo,
        UUID createdByUserId,
        String createdBy,
        OffsetDateTime createdAt) {
}
