package cd.edrc.landgis.notifications;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {
    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;

    public NotificationService(JdbcTemplate jdbcTemplate, AuditService auditService) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
    }

    @Transactional
    public void createForUser(
            UUID recipientUserId,
            String type,
            String title,
            String message,
            String targetType,
            UUID targetId,
            String dedupeKey,
            AuthenticatedActor actor) {
        jdbcTemplate.update(
                """
                INSERT INTO notifications.inbox
                    (recipient_user_id, notification_type, title, message, target_type, target_id, dedupe_key)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (dedupe_key) DO NOTHING
                """,
                recipientUserId, type, title, message, targetType, targetId, dedupeKey);
        auditService.record(
                "notification.created",
                "notification",
                dedupeKey,
                AuditClassification.STAFF_OPERATIONAL,
                actor == null ? null : actor.userId(),
                null,
                java.util.Map.of("notificationType", type, "recipientUserId", recipientUserId.toString()));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> listForActor(AuthenticatedActor actor) {
        return jdbcTemplate.query(
                """
                SELECT id, notification_type, title, message, target_type, target_id,
                       read_at, created_at
                FROM notifications.inbox
                WHERE recipient_user_id = ?
                ORDER BY created_at DESC
                LIMIT 100
                """,
                mapper(), actor.userId());
    }

    @Transactional
    public void markRead(UUID notificationId, AuthenticatedActor actor) {
        int updated = jdbcTemplate.update(
                "UPDATE notifications.inbox SET read_at = COALESCE(read_at, now()) WHERE id = ? AND recipient_user_id = ?",
                notificationId, actor.userId());
        if (updated == 0) {
            throw new IllegalArgumentException("Notification not found for actor: " + notificationId);
        }
        auditService.record(
                "notification.read",
                "notification",
                notificationId.toString(),
                AuditClassification.STAFF_OPERATIONAL,
                actor.userId(),
                null,
                java.util.Map.of("recipientUserId", actor.userId().toString()));
    }

    private RowMapper<NotificationResponse> mapper() {
        return (resultSet, rowNum) -> {
            OffsetDateTime readAt = resultSet.getObject("read_at", OffsetDateTime.class);
            return new NotificationResponse(
                    resultSet.getObject("id", UUID.class),
                    resultSet.getString("notification_type"),
                    resultSet.getString("title"),
                    resultSet.getString("message"),
                    resultSet.getString("target_type"),
                    resultSet.getObject("target_id", UUID.class),
                    readAt != null,
                    resultSet.getObject("created_at", OffsetDateTime.class),
                    readAt);
        };
    }
}
