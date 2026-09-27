package cd.edrc.landgis.governance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "governance", name = "export_packages")
public class DocumentExportPackage {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID exportRequestId;

    @Column(nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private UUID documentVersionId;

    @Column(nullable = false)
    private String objectStorageKey;

    @Column(nullable = false)
    private String manifestSha256;

    @Column(nullable = false)
    private String packageSha256;

    @Column(nullable = false)
    private long packageSizeBytes;

    @Column(nullable = false)
    private String tokenSha256;

    @Column(nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private UUID generatedByUserId;

    @Column(nullable = false)
    private String generatedBy;

    @Column(nullable = false)
    private OffsetDateTime generatedAt;

    private OffsetDateTime downloadedAt;
    private UUID lastDownloadedByUserId;
    private String lastDownloadedBy;

    @Column(nullable = false)
    private int downloadCount;

    @Version
    private long version;

    protected DocumentExportPackage() {
    }

    public DocumentExportPackage(
            UUID id,
            UUID exportRequestId,
            UUID documentId,
            UUID documentVersionId,
            String objectStorageKey,
            String manifestSha256,
            String packageSha256,
            long packageSizeBytes,
            String tokenSha256,
            OffsetDateTime expiresAt,
            UUID generatedByUserId,
            String generatedBy) {
        this.id = id;
        this.exportRequestId = exportRequestId;
        this.documentId = documentId;
        this.documentVersionId = documentVersionId;
        this.objectStorageKey = objectStorageKey;
        this.manifestSha256 = manifestSha256;
        this.packageSha256 = packageSha256;
        this.packageSizeBytes = packageSizeBytes;
        this.tokenSha256 = tokenSha256;
        this.expiresAt = expiresAt;
        this.generatedByUserId = generatedByUserId;
        this.generatedBy = generatedBy;
        this.generatedAt = OffsetDateTime.now();
        this.downloadCount = 0;
    }

    public void recordDownload(UUID actorUserId, String actorUsername) {
        this.downloadedAt = OffsetDateTime.now();
        this.lastDownloadedByUserId = actorUserId;
        this.lastDownloadedBy = actorUsername;
        this.downloadCount++;
    }

    public UUID id() {
        return id;
    }

    public UUID exportRequestId() {
        return exportRequestId;
    }

    public UUID documentId() {
        return documentId;
    }

    public UUID documentVersionId() {
        return documentVersionId;
    }

    public String objectStorageKey() {
        return objectStorageKey;
    }

    public String manifestSha256() {
        return manifestSha256;
    }

    public String packageSha256() {
        return packageSha256;
    }

    public long packageSizeBytes() {
        return packageSizeBytes;
    }

    public OffsetDateTime expiresAt() {
        return expiresAt;
    }

    public UUID generatedByUserId() {
        return generatedByUserId;
    }

    public String generatedBy() {
        return generatedBy;
    }

    public OffsetDateTime generatedAt() {
        return generatedAt;
    }

    public OffsetDateTime downloadedAt() {
        return downloadedAt;
    }

    public UUID lastDownloadedByUserId() {
        return lastDownloadedByUserId;
    }

    public String lastDownloadedBy() {
        return lastDownloadedBy;
    }

    public int downloadCount() {
        return downloadCount;
    }

    String tokenSha256() {
        return tokenSha256;
    }
}
