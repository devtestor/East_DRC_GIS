package cd.edrc.landgis.disputes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record CreateParcelRestrictionRequest(
        @NotNull RestrictionType restrictionType,
        @NotBlank @Size(max = 120) String source,
        @Size(max = 120) String authorityReference,
        @NotBlank @Size(max = 500) String summary,
        boolean blocksOwnershipChanges,
        OffsetDateTime effectiveTo) {
}
