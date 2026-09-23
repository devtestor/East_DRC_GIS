package cd.edrc.landgis.parcels;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateParcelGeometryVersionRequest(
        @NotBlank @Size(max = 200_000) String geometryWkt,
        @NotBlank @Size(max = 120) String source) {
}
