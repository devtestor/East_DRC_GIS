package cd.edrc.landgis.audit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateRegisteredDeviceRequest(
        @NotBlank
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9._:-]{2,79}$")
        String deviceId,
        UUID assignedUserId,
        UUID organizationId,
        @NotNull RegisteredDeviceType deviceType,
        OffsetDateTime expiresAt) {
}
