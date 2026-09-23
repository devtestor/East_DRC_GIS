package cd.edrc.landgis.surveys;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BoundaryAcknowledgementRequest(
        @NotBlank @Size(max = 200) String neighborName,
        @NotNull BoundaryAcknowledgementStatus status,
        @Size(max = 1000) String note) {
}
