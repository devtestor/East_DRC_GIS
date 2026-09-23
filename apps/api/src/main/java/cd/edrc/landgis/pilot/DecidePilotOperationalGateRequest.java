package cd.edrc.landgis.pilot;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DecidePilotOperationalGateRequest(
        @NotNull PilotOperationalGateStatus status,
        @Size(max = 240) String evidenceReference) {
}
