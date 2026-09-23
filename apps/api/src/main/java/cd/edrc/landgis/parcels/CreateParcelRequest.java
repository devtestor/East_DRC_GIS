package cd.edrc.landgis.parcels;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

record CreateParcelRequest(
        @NotNull UUID administrativeUnitId,
        @NotBlank @Pattern(regexp = "[A-Z0-9.-]{6,64}") String proposedUpi,
        @Size(max = 120) String landUse,
        @Size(max = 120) String tenureClassification) {
}
