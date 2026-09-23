package cd.edrc.landgis.parcels;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ParcelGeometryVersionResponse(
        UUID id,
        UUID parcelId,
        String geometryWkt,
        String geometryStatus,
        BigDecimal calculatedAreaSquareMeters,
        String source,
        OffsetDateTime effectiveFrom,
        OffsetDateTime effectiveTo,
        UUID approvedBy,
        OffsetDateTime approvedAt,
        OffsetDateTime createdAt) {
}
