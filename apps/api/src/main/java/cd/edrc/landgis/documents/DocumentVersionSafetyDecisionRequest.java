package cd.edrc.landgis.documents;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentVersionSafetyDecisionRequest(
        @NotBlank @Size(max = 500) String reason) {
}
