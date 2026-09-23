package cd.edrc.landgis.documents;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "documents", name = "documents")
public class DocumentRecord {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String documentType;

    @Column(nullable = false)
    private String ownerType;

    @Column(nullable = false)
    private UUID ownerId;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentClassification classification;

    @Column(nullable = false)
    private String retentionCategory;

    @Column(nullable = false)
    private String accessPolicy;

    private UUID custodianOrganizationId;

    private String custodianRoleCode;

    @Column(nullable = false)
    private boolean legalHold;

    @Column(nullable = false)
    private UUID createdByUserId;

    @Column(nullable = false)
    private String createdBy;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    private OffsetDateTime effectiveAt;

    @Version
    private long version;

    protected DocumentRecord() {
    }

    public DocumentRecord(
            UUID id,
            String documentType,
            String ownerType,
            UUID ownerId,
            String title,
            DocumentClassification classification,
            String retentionCategory,
            String accessPolicy,
            UUID custodianOrganizationId,
            String custodianRoleCode,
            boolean legalHold,
            UUID createdByUserId,
            String createdBy,
            OffsetDateTime effectiveAt) {
        this.id = id;
        this.documentType = documentType;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.title = title;
        this.classification = classification;
        this.retentionCategory = retentionCategory;
        this.accessPolicy = accessPolicy;
        this.custodianOrganizationId = custodianOrganizationId;
        this.custodianRoleCode = custodianRoleCode;
        this.legalHold = legalHold;
        this.createdByUserId = createdByUserId;
        this.createdBy = createdBy;
        this.createdAt = OffsetDateTime.now();
        this.effectiveAt = effectiveAt;
    }

    public UUID id() {
        return id;
    }

    public String documentType() {
        return documentType;
    }

    public String ownerType() {
        return ownerType;
    }

    public UUID ownerId() {
        return ownerId;
    }

    public String title() {
        return title;
    }

    public DocumentClassification classification() {
        return classification;
    }

    public String retentionCategory() {
        return retentionCategory;
    }

    public String accessPolicy() {
        return accessPolicy;
    }

    public UUID custodianOrganizationId() {
        return custodianOrganizationId;
    }

    public String custodianRoleCode() {
        return custodianRoleCode;
    }

    public boolean legalHold() {
        return legalHold;
    }

    public UUID createdByUserId() {
        return createdByUserId;
    }

    public String createdBy() {
        return createdBy;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }

    public OffsetDateTime effectiveAt() {
        return effectiveAt;
    }
}
