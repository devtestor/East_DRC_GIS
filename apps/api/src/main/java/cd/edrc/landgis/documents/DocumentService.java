package cd.edrc.landgis.documents;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentService {
    private final DocumentRecordRepository documents;
    private final DocumentVersionRecordRepository versions;
    private final DocumentAccessAuthorizer access;
    private final AuditService auditService;
    private final DocumentContentStore contentStore;

    DocumentService(
            DocumentRecordRepository documents,
            DocumentVersionRecordRepository versions,
            DocumentAccessAuthorizer access,
            AuditService auditService) {
        this(documents, versions, access, auditService, null);
    }

    @Autowired
    public DocumentService(
            DocumentRecordRepository documents,
            DocumentVersionRecordRepository versions,
            DocumentAccessAuthorizer access,
            AuditService auditService,
            DocumentContentStore contentStore) {
        this.documents = documents;
        this.versions = versions;
        this.access = access;
        this.auditService = auditService;
        this.contentStore = contentStore;
    }

    @Transactional
    public DocumentResponse create(CreateDocumentRequest request, AuthenticatedActor actor) {
        String checksum = request.checksumSha256().trim().toLowerCase(Locale.ROOT);
        String custodianRoleCode = normalizeOptionalUppercase(request.custodianRoleCode());
        DocumentRecord document = documents.save(new DocumentRecord(
                UUID.randomUUID(),
                request.documentType(),
                request.ownerType(),
                request.ownerId(),
                request.title(),
                request.classification(),
                request.retentionCategory(),
                request.accessPolicy(),
                request.custodianOrganizationId(),
                custodianRoleCode,
                request.legalHold(),
                actor.userId(),
                actor.username(),
                request.effectiveAt()));
        DocumentVersionRecord firstVersion = versions.save(new DocumentVersionRecord(
                UUID.randomUUID(),
                document.id(),
                1,
                request.objectStorageKey(),
                request.originalFilename(),
                request.mediaType(),
                request.sizeBytes(),
                checksum,
                request.malwareScanStatus(),
                request.digitalSignatureStatus(),
                actor.userId(),
                actor.username()));
        auditService.record(
                "document.metadata-created",
                "document",
                document.id().toString(),
                auditClassificationFor(document),
                actor.userId(),
                document.custodianOrganizationId(),
                auditEvidence(document, "create"));
        return DocumentResponse.from(document, List.of(firstVersion));
    }

    @Transactional(readOnly = true)
    public DocumentResponse get(UUID documentId, AuthenticatedActor actor) {
        DocumentRecord document = documents.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        requireReadAccess(document, actor);
        auditService.record(
                "document.metadata-read",
                "document",
                document.id().toString(),
                auditClassificationFor(document),
                actor.userId(),
                document.custodianOrganizationId(),
                auditEvidence(document, "read"));
        return DocumentResponse.from(document, versions.findByDocumentIdOrderByVersionNumberAsc(documentId));
    }

    @Transactional(readOnly = true)
    public DocumentDownload download(UUID documentId, AuthenticatedActor actor) {
        DocumentRecord document = documents.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        requireReadAccess(document, actor);
        List<DocumentVersionRecord> documentVersions = versions.findByDocumentIdOrderByVersionNumberAsc(documentId);
        if (documentVersions.isEmpty() || contentStore == null) {
            throw new IllegalStateException("Document content is not available in this environment");
        }
        DocumentVersionRecord version = documentVersions.getLast();
        auditService.record("document.content-downloaded", "document", document.id().toString(),
                auditClassificationFor(document), actor.userId(), document.custodianOrganizationId(),
                auditEvidence(document, "download"));
        return new DocumentDownload(contentStore.read(document, version),
                version.originalFilename().replaceFirst("\\.pdf$", "-sandbox.txt"), "text/plain");
    }

    @Transactional
    public DocumentResponse addVersion(UUID documentId, AddDocumentVersionRequest request, AuthenticatedActor actor) {
        DocumentRecord document = documents.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        if (!access.canAppendVersion(document, actor)) {
            auditService.record(
                    "document.version-append-denied",
                    "document",
                    document.id().toString(),
                    AuditClassification.SECURITY,
                    actor.userId(),
                    document.custodianOrganizationId(),
                    auditEvidence(document, "append-denied"));
            throw new DocumentAccessDeniedException(documentId);
        }
        int nextVersion = versions.countByDocumentId(documentId) + 1;
        versions.save(new DocumentVersionRecord(
                UUID.randomUUID(),
                documentId,
                nextVersion,
                request.objectStorageKey(),
                request.originalFilename(),
                request.mediaType(),
                request.sizeBytes(),
                request.checksumSha256().trim().toLowerCase(Locale.ROOT),
                request.malwareScanStatus(),
                request.digitalSignatureStatus(),
                actor.userId(),
                actor.username()));
        auditService.record(
                "document.version-appended",
                "document",
                document.id().toString(),
                auditClassificationFor(document),
                actor.userId(),
                document.custodianOrganizationId(),
                auditEvidence(document, "append"));
        return DocumentResponse.from(document, versions.findByDocumentIdOrderByVersionNumberAsc(documentId));
    }

    @Transactional
    public DocumentResponse updateVersionSafetyStatus(
            UUID documentId,
            UUID versionId,
            UpdateDocumentVersionSafetyStatusRequest request,
            AuthenticatedActor actor) {
        DocumentRecord document = documents.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        if (!access.canAppendVersion(document, actor)) {
            auditService.record(
                    "document.version-safety-update-denied",
                    "document-version",
                    versionId.toString(),
                    AuditClassification.SECURITY,
                    actor.userId(),
                    document.custodianOrganizationId(),
                    auditEvidence(document, "safety-update-denied"));
            throw new DocumentAccessDeniedException(documentId);
        }
        DocumentVersionRecord version = versions.findByIdAndDocumentId(versionId, documentId)
                .orElseThrow(() -> new DocumentVersionNotFoundException(documentId, versionId));
        MalwareScanStatus previousMalwareScanStatus = version.malwareScanStatus();
        DigitalSignatureStatus previousDigitalSignatureStatus = version.digitalSignatureStatus();
        version.updateSafetyStatus(request.malwareScanStatus(), request.digitalSignatureStatus());
        versions.save(version);
        auditService.record(
                "document.version-safety-status-updated",
                "document-version",
                version.id().toString(),
                auditClassificationFor(document),
                actor.userId(),
                document.custodianOrganizationId(),
                safetyAuditEvidence(
                        document,
                        version,
                        previousMalwareScanStatus,
                        previousDigitalSignatureStatus,
                        request));
        return DocumentResponse.from(document, versions.findByDocumentIdOrderByVersionNumberAsc(documentId));
    }

    @Transactional
    public DocumentResponse quarantineVersion(
            UUID documentId,
            UUID versionId,
            DocumentVersionSafetyDecisionRequest request,
            AuthenticatedActor actor) {
        DocumentRecord document = documents.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        if (!access.canAppendVersion(document, actor)) {
            auditService.record(
                    "document.version-quarantine-denied",
                    "document-version",
                    versionId.toString(),
                    AuditClassification.SECURITY,
                    actor.userId(),
                    document.custodianOrganizationId(),
                    auditEvidence(document, "quarantine-denied"));
            throw new DocumentAccessDeniedException(documentId);
        }
        DocumentVersionRecord version = versions.findByIdAndDocumentId(versionId, documentId)
                .orElseThrow(() -> new DocumentVersionNotFoundException(documentId, versionId));
        DocumentVersionSafetyStatus previousStatus = version.safetyStatus();
        version.quarantine(request.reason(), actor.userId(), actor.username());
        versions.save(version);
        auditService.record(
                "document.version-quarantined",
                "document-version",
                version.id().toString(),
                auditClassificationFor(document),
                actor.userId(),
                document.custodianOrganizationId(),
                safetyDecisionAuditEvidence(document, version, previousStatus, request.reason(), "quarantine"));
        return DocumentResponse.from(document, versions.findByDocumentIdOrderByVersionNumberAsc(documentId));
    }

    @Transactional
    public DocumentResponse releaseVersion(
            UUID documentId,
            UUID versionId,
            DocumentVersionSafetyDecisionRequest request,
            AuthenticatedActor actor) {
        DocumentRecord document = documents.findById(documentId)
                .orElseThrow(() -> new DocumentNotFoundException(documentId));
        if (!access.canAppendVersion(document, actor)) {
            auditService.record(
                    "document.version-release-denied",
                    "document-version",
                    versionId.toString(),
                    AuditClassification.SECURITY,
                    actor.userId(),
                    document.custodianOrganizationId(),
                    auditEvidence(document, "release-denied"));
            throw new DocumentAccessDeniedException(documentId);
        }
        DocumentVersionRecord version = versions.findByIdAndDocumentId(versionId, documentId)
                .orElseThrow(() -> new DocumentVersionNotFoundException(documentId, versionId));
        if (version.malwareScanStatus() == MalwareScanStatus.FAILED
                || version.digitalSignatureStatus() == DigitalSignatureStatus.INVALID) {
            auditService.record(
                    "document.version-release-blocked",
                    "document-version",
                    version.id().toString(),
                    AuditClassification.SECURITY,
                    actor.userId(),
                    document.custodianOrganizationId(),
                    safetyDecisionAuditEvidence(
                            document,
                            version,
                            version.safetyStatus(),
                            request.reason(),
                            "release-blocked"));
            throw new UnsafeDocumentVersionReleaseException(documentId, versionId);
        }
        DocumentVersionSafetyStatus previousStatus = version.safetyStatus();
        version.releaseFromQuarantine(request.reason(), actor.userId(), actor.username());
        versions.save(version);
        auditService.record(
                "document.version-released",
                "document-version",
                version.id().toString(),
                auditClassificationFor(document),
                actor.userId(),
                document.custodianOrganizationId(),
                safetyDecisionAuditEvidence(document, version, previousStatus, request.reason(), "release"));
        return DocumentResponse.from(document, versions.findByDocumentIdOrderByVersionNumberAsc(documentId));
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> findByOwner(String ownerType, UUID ownerId, AuthenticatedActor actor) {
        auditService.record(
                "document.owner-list-viewed",
                "document-owner",
                ownerType + ":" + ownerId,
                AuditClassification.STAFF_OPERATIONAL,
                actor.userId(),
                null,
                Map.of("ownerType", ownerType, "operation", "owner-list"));
        return documents.findByOwnerTypeAndOwnerIdOrderByCreatedAtDesc(ownerType, ownerId).stream()
                .filter(document -> access.canRead(document, actor))
                .map(document -> DocumentResponse.from(
                        document,
                        versions.findByDocumentIdOrderByVersionNumberAsc(document.id())))
                .toList();
    }

    private void requireReadAccess(DocumentRecord document, AuthenticatedActor actor) {
        if (!access.canRead(document, actor)) {
            auditService.record(
                    "document.metadata-read-denied",
                    "document",
                    document.id().toString(),
                    AuditClassification.SECURITY,
                    actor.userId(),
                    document.custodianOrganizationId(),
                    auditEvidence(document, "read-denied"));
            throw new DocumentAccessDeniedException(document.id());
        }
    }

    private AuditClassification auditClassificationFor(DocumentRecord document) {
        return AuditClassification.valueOf(document.classification().name());
    }

    private Map<String, Object> auditEvidence(DocumentRecord document, String operation) {
        return Map.of(
                "documentType", document.documentType(),
                "ownerType", document.ownerType(),
                "classification", document.classification().name(),
                "accessPolicy", document.accessPolicy(),
                "custodianScoped", document.custodianOrganizationId() != null,
                "operation", operation);
    }

    private Map<String, Object> safetyAuditEvidence(
            DocumentRecord document,
            DocumentVersionRecord version,
            MalwareScanStatus previousMalwareScanStatus,
            DigitalSignatureStatus previousDigitalSignatureStatus,
            UpdateDocumentVersionSafetyStatusRequest request) {
        Map<String, Object> evidence = new HashMap<>(auditEvidence(document, "safety-status-update"));
        evidence.put("documentId", document.id().toString());
        evidence.put("documentVersionId", version.id().toString());
        evidence.put("versionNumber", version.versionNumber());
        evidence.put("previousMalwareScanStatus", previousMalwareScanStatus.name());
        evidence.put("newMalwareScanStatus", request.malwareScanStatus().name());
        evidence.put("previousDigitalSignatureStatus", previousDigitalSignatureStatus.name());
        evidence.put("newDigitalSignatureStatus", request.digitalSignatureStatus().name());
        evidence.put("reason", request.reason());
        return evidence;
    }

    private Map<String, Object> safetyDecisionAuditEvidence(
            DocumentRecord document,
            DocumentVersionRecord version,
            DocumentVersionSafetyStatus previousStatus,
            String reason,
            String operation) {
        Map<String, Object> evidence = new HashMap<>(auditEvidence(document, operation));
        evidence.put("documentId", document.id().toString());
        evidence.put("documentVersionId", version.id().toString());
        evidence.put("versionNumber", version.versionNumber());
        evidence.put("previousSafetyStatus", previousStatus.name());
        evidence.put("newSafetyStatus", version.safetyStatus().name());
        evidence.put("malwareScanStatus", version.malwareScanStatus().name());
        evidence.put("digitalSignatureStatus", version.digitalSignatureStatus().name());
        evidence.put("reason", reason);
        return evidence;
    }

    private String normalizeOptionalUppercase(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
