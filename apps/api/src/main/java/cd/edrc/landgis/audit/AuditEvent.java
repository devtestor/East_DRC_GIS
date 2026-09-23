package cd.edrc.landgis.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.net.InetAddress;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Immutable
@Table(schema = "audit", name = "audit_events")
public class AuditEvent {
    @Id
    private UUID id;

    @Column(nullable = false)
    private OffsetDateTime eventTime;

    @Column(nullable = false)
    private String correlationId;

    private UUID actorUserId;

    private UUID actorOrganizationId;

    private InetAddress actorIp;

    private String actorUserAgent;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String targetType;

    private String targetId;

    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> evidence;

    @Column(nullable = false)
    private String sourceService;

    private String sourceDeviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditClassification classification;

    protected AuditEvent() {
    }

    public AuditEvent(
            UUID id,
            OffsetDateTime eventTime,
            String correlationId,
            UUID actorUserId,
            UUID actorOrganizationId,
            InetAddress actorIp,
            String actorUserAgent,
            String action,
            String targetType,
            String targetId,
            Map<String, Object> evidence,
            String sourceService,
            String sourceDeviceId,
            AuditClassification classification) {
        this.id = id;
        this.eventTime = eventTime;
        this.correlationId = correlationId;
        this.actorUserId = actorUserId;
        this.actorOrganizationId = actorOrganizationId;
        this.actorIp = actorIp;
        this.actorUserAgent = actorUserAgent;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.evidence = evidence;
        this.sourceService = sourceService;
        this.sourceDeviceId = sourceDeviceId;
        this.classification = classification;
    }

    public UUID id() {
        return id;
    }

    public String correlationId() {
        return correlationId;
    }

    public UUID actorUserId() {
        return actorUserId;
    }

    public UUID actorOrganizationId() {
        return actorOrganizationId;
    }

    public InetAddress actorIp() {
        return actorIp;
    }

    public String actorUserAgent() {
        return actorUserAgent;
    }

    public String action() {
        return action;
    }

    public String targetType() {
        return targetType;
    }

    public String targetId() {
        return targetId;
    }

    public Map<String, Object> evidence() {
        return evidence;
    }

    public AuditClassification classification() {
        return classification;
    }

    public String sourceDeviceId() {
        return sourceDeviceId;
    }
}
