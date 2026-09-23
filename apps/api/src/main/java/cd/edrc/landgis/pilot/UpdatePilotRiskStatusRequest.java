package cd.edrc.landgis.pilot;

import jakarta.validation.constraints.NotNull;

public record UpdatePilotRiskStatusRequest(
        @NotNull PilotRiskStatus status,
        boolean blockingGoLive) {
}
