package cd.edrc.landgis.documents;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "documents", name = "document_versions")
public class DocumentVersionRecord {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID documentId;

    @Column(nullable = false)
    private int versionNumber;

    @Column(nullable = false)
    private String objectStorageKey;

    @Column(nullable = false)
    private String originalFilename;

    @Column(nullable = false)
    private String mediaType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column(nullable = false)
    private String checksumSha256;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MalwareScanStatus malwareScanStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DigitalSignatureStatus digitalSignatureStatus;

    @Column(nullable = false)
    private UUID uploadedByUserId;

    @Column(nullable = false)
    private String uploadedBy;

    @Column(nullable = false)
    private OffsetDateTime uploadedAt;

    protected DocumentVersionRecord() {
    }

    public DocumentVersionRecord(
            UUID id,
            UUID documentId,
            int versionNumber,
            String objectStorageKey,
            String originalFilename,
            String mediaType,
            long sizeBytes,
            String checksumSha256,
            MalwareScanStatus malwareScanStatus,
            DigitalSignatureStatus digitalSignatureStatus,
            UUID uploadedByUserId,
            String uploadedBy) {
        this.id = id;
        this.documentId = documentId;
        this.versionNumber = versionNumber;
        this.objectStorageKey = objectStorageKey;
        this.originalFilename = originalFilename;
        this.mediaType = mediaType;
        this.sizeBytes = sizeBytes;
        this.checksumSha256 = checksumSha256;
        this.malwareScanStatus = malwareScanStatus;
        this.digitalSignatureStatus = digitalSignatureStatus;
        this.uploadedByUserId = uploadedByUserId;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = OffsetDateTime.now();
    }

    public UUID id() {
        return id;
    }

    public UUID documentId() {
        return documentId;
    }

    public int versionNumber() {
        return versionNumber;
    }

    public String objectStorageKey() {
        return objectStorageKey;
    }

    public String originalFilename() {
        return originalFilename;
    }

    public String mediaType() {
        return mediaType;
    }

    public long sizeBytes() {
        return sizeBytes;
    }

    public String checksumSha256() {
        return checksumSha256;
    }

    public MalwareScanStatus malwareScanStatus() {
        return malwareScanStatus;
    }

    public DigitalSignatureStatus digitalSignatureStatus() {
        return digitalSignatureStatus;
    }

    public UUID uploadedByUserId() {
        return uploadedByUserId;
    }

    public String uploadedBy() {
        return uploadedBy;
    }

    public OffsetDateTime uploadedAt() {
        return uploadedAt;
    }
}
