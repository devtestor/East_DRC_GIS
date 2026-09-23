package cd.edrc.landgis.rights;

import java.util.UUID;

public record OwnershipConflictResponse(
        UUID parcelId,
        String conflictType,
        String severity,
        int affectedInterestCount,
        String summary) {
}
