package cd.edrc.landgis.disputes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDisputeAppealRequest(
        @NotNull DisputeAppealType appealType,
        @NotBlank @Size(max = 3000) String grounds) {
}
