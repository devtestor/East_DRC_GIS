package cd.edrc.landgis.audit;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RegisteredDeviceResponse(
        UUID id,
        String deviceId,
        UUID assignedUserId,
        UUID organizationId,
        String deviceType,
        String status,
        OffsetDateTime enrolledAt,
        OffsetDateTime expiresAt,
        OffsetDateTime revokedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt) {
    static RegisteredDeviceResponse from(RegisteredDevice device) {
        return new RegisteredDeviceResponse(
                device.id(),
                device.deviceId(),
                device.assignedUserId(),
                device.organizationId(),
                device.deviceType().name(),
                device.status().name(),
                device.enrolledAt(),
                device.expiresAt(),
                device.revokedAt(),
                device.createdAt(),
                device.updatedAt());
    }
}
