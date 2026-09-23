package cd.edrc.landgis.pilot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePilotOperationalGateRequest(
        @NotNull PilotOperationalGateType gateType,
        @NotBlank @Pattern(regexp = "[A-Z0-9_]{3,80}") String ownerRole,
        @NotBlank @Size(max = 500) String summary,
        @Size(max = 240) String evidenceReference) {
}
