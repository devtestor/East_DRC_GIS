package cd.edrc.landgis.notifications;

import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        String notificationType,
        String title,
        String message,
        String targetType,
        UUID targetId,
        boolean read,
        OffsetDateTime createdAt,
        OffsetDateTime readAt) {
}
