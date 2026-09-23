package cd.edrc.landgis.disputes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateDisputeCaseRequest(
        @NotNull DisputeCaseType caseType,
        @NotBlank @Size(max = 2000) String summary,
        @NotBlank @Size(max = 200) String source,
        @Size(max = 200) String authorityReference) {
}
