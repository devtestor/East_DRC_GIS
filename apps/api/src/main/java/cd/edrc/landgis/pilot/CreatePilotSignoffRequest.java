package cd.edrc.landgis.pilot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreatePilotSignoffRequest(
        @NotNull PilotSignoffType signoffType,
        @NotBlank @Pattern(regexp = "[A-Z0-9_]{3,80}") String requiredRole,
        @NotBlank @Size(max = 500) String summary) {
}
