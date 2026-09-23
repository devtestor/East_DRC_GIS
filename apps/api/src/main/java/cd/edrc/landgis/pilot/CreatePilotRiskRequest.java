package cd.edrc.landgis.pilot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreatePilotRiskRequest(
        @NotNull PilotRiskSeverity severity,
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 700) String mitigationPlan,
        boolean blockingGoLive) {
}
