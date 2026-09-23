package cd.edrc.landgis.pilot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreatePilotEvidenceRequest(
        @NotNull PilotEvidenceType evidenceType,
        @NotBlank @Size(max = 80) String referenceType,
        UUID referenceId,
        @Size(max = 240) String externalReference,
        @NotBlank @Size(max = 500) String summary) {
}
