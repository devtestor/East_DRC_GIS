package cd.edrc.landgis.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "identity", name = "registered_devices")
public class RegisteredDevice {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String deviceId;

    private UUID assignedUserId;

    private UUID organizationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegisteredDeviceType deviceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RegisteredDeviceStatus status;

    @Column(nullable = false)
    private OffsetDateTime enrolledAt;

    private OffsetDateTime expiresAt;

    private OffsetDateTime revokedAt;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    @Version
    private long version;

    protected RegisteredDevice() {
    }

    public RegisteredDevice(
            UUID id,
            String deviceId,
            UUID assignedUserId,
            UUID organizationId,
            RegisteredDeviceType deviceType,
            OffsetDateTime expiresAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.assignedUserId = assignedUserId;
        this.organizationId = organizationId;
        this.deviceType = deviceType;
        this.status = RegisteredDeviceStatus.ACTIVE;
        this.enrolledAt = OffsetDateTime.now();
        this.expiresAt = expiresAt;
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    public void suspend() {
        this.status = RegisteredDeviceStatus.SUSPENDED;
    }

    public void revoke() {
        this.status = RegisteredDeviceStatus.REVOKED;
        this.revokedAt = OffsetDateTime.now();
    }

    public void expire() {
        this.status = RegisteredDeviceStatus.EXPIRED;
        this.expiresAt = OffsetDateTime.now();
    }

    public UUID id() {
        return id;
    }

    public String deviceId() {
        return deviceId;
    }

    public UUID assignedUserId() {
        return assignedUserId;
    }

    public UUID organizationId() {
        return organizationId;
    }

    public RegisteredDeviceType deviceType() {
        return deviceType;
    }

    public RegisteredDeviceStatus status() {
        return status;
    }

    public OffsetDateTime enrolledAt() {
        return enrolledAt;
    }

    public OffsetDateTime expiresAt() {
        return expiresAt;
    }

    public OffsetDateTime revokedAt() {
        return revokedAt;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }

    public OffsetDateTime updatedAt() {
        return updatedAt;
    }
}
