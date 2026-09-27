package cd.edrc.landgis.governance;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.documents.DocumentContentStore;
import cd.edrc.landgis.documents.DocumentRecord;
import cd.edrc.landgis.documents.DocumentRecordRepository;
import cd.edrc.landgis.documents.DocumentVersionRecord;
import cd.edrc.landgis.documents.DocumentVersionRecordRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentExportPackageService {
    private static final SecureRandom TOKEN_RANDOM = new SecureRandom();

    private final DocumentExportRequestRepository exportRequests;
    private final DocumentExportPackageRepository exportPackages;
    private final DocumentRecordRepository documents;
    private final DocumentVersionRecordRepository versions;
    private final DocumentContentStore contentStore;
    private final AuditService auditService;

    public DocumentExportPackageService(
            DocumentExportRequestRepository exportRequests,
            DocumentExportPackageRepository exportPackages,
            DocumentRecordRepository documents,
            DocumentVersionRecordRepository versions,
            DocumentContentStore contentStore,
            AuditService auditService) {
        this.exportRequests = exportRequests;
        this.exportPackages = exportPackages;
        this.documents = documents;
        this.versions = versions;
        this.contentStore = contentStore;
        this.auditService = auditService;
    }

    @Transactional
    public DocumentExportPackageResponse generate(
            UUID exportRequestId,
            CreateDocumentExportPackageRequest request,
            AuthenticatedActor actor) {
        DocumentExportRequest exportRequest = exportRequests.findById(exportRequestId)
                .orElseThrow(() -> new DocumentExportRequestNotFoundException(exportRequestId));
        if (exportRequest.status() != ExportRequestStatus.APPROVED) {
            throw new DocumentExportPackageException("Export package can only be generated for an approved request");
        }
        if (exportPackages.existsByExportRequestId(exportRequestId)) {
            throw new DocumentExportPackageException("Export package already exists for this request");
        }
        DocumentRecord document = documents.findById(exportRequest.documentId())
                .orElseThrow(() -> new cd.edrc.landgis.documents.DocumentNotFoundException(exportRequest.documentId()));
        DocumentVersionRecord version = latestVersion(document.id());
        byte[] manifest = manifest(exportRequest, document, version);
        byte[] sourceContent = contentStore.read(document, version);
        byte[] packageContent = packageContent(manifest, sourceContent);
        String token = newToken();
        DocumentExportPackage exportPackage = exportPackages.save(new DocumentExportPackage(
                UUID.randomUUID(),
                exportRequest.id(),
                document.id(),
                version.id(),
                "sandbox/governed-exports/" + exportRequest.id() + ".txt",
                sha256(manifest),
                sha256(packageContent),
                packageContent.length,
                sha256(token.getBytes(StandardCharsets.UTF_8)),
                OffsetDateTime.now().plusMinutes(request.expiresInMinutes()),
                actor.userId(),
                actor.username()));
        auditService.record(
                "governance.document-export-package-generated",
                "document-export-package",
                exportPackage.id().toString(),
                AuditClassification.SECURITY,
                actor.userId(),
                document.custodianOrganizationId(),
                Map.of(
                        "exportRequestId", exportRequest.id().toString(),
                        "documentId", document.id().toString(),
                        "documentVersionId", version.id().toString(),
                        "packageSha256", exportPackage.packageSha256(),
                        "expiresAt", exportPackage.expiresAt().toString()));
        return DocumentExportPackageResponse.from(exportPackage, token);
    }

    @Transactional(readOnly = true)
    public DocumentExportPackageResponse get(UUID packageId) {
        return DocumentExportPackageResponse.from(exportPackage(packageId), null);
    }

    @Transactional
    public DocumentExportPackageDownload download(UUID packageId, String token, AuthenticatedActor actor) {
        DocumentExportPackage exportPackage = exportPackage(packageId);
        if (OffsetDateTime.now().isAfter(exportPackage.expiresAt())) {
            throw new DocumentExportPackageException("Export package delivery token has expired");
        }
        if (token == null || token.isBlank()
                || !MessageDigest.isEqual(
                exportPackage.tokenSha256().getBytes(StandardCharsets.UTF_8),
                sha256(token.getBytes(StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8))) {
            throw new DocumentExportPackageException("Invalid export package delivery token");
        }
        DocumentRecord document = documents.findById(exportPackage.documentId())
                .orElseThrow(() -> new cd.edrc.landgis.documents.DocumentNotFoundException(exportPackage.documentId()));
        DocumentVersionRecord version = versions.findById(exportPackage.documentVersionId())
                .orElseThrow(() -> new DocumentExportPackageException("Export source document version is missing"));
        DocumentExportRequest exportRequest = exportRequests.findById(exportPackage.exportRequestId())
                .orElseThrow(() -> new DocumentExportRequestNotFoundException(exportPackage.exportRequestId()));
        byte[] packageContent = packageContent(
                manifest(exportRequest, document, version),
                contentStore.read(document, version));
        String actualChecksum = sha256(packageContent);
        if (!actualChecksum.equals(exportPackage.packageSha256())) {
            throw new DocumentExportPackageException("Export package checksum verification failed");
        }
        exportPackage.recordDownload(actor.userId(), actor.username());
        auditService.record(
                "governance.document-export-package-downloaded",
                "document-export-package",
                exportPackage.id().toString(),
                AuditClassification.SECURITY,
                actor.userId(),
                document.custodianOrganizationId(),
                Map.of(
                        "exportRequestId", exportPackage.exportRequestId().toString(),
                        "documentId", document.id().toString(),
                        "packageSha256", exportPackage.packageSha256(),
                        "downloadCount", exportPackage.downloadCount()));
        return new DocumentExportPackageDownload(
                packageContent,
                "edrc-governed-export-" + exportPackage.id() + ".txt",
                "text/plain");
    }

    private DocumentExportPackage exportPackage(UUID packageId) {
        return exportPackages.findById(packageId)
                .orElseThrow(() -> new DocumentExportPackageNotFoundException(packageId));
    }

    private DocumentVersionRecord latestVersion(UUID documentId) {
        List<DocumentVersionRecord> documentVersions = versions.findByDocumentIdOrderByVersionNumberAsc(documentId);
        if (documentVersions.isEmpty()) {
            throw new DocumentExportPackageException("Document has no content version to export");
        }
        return documentVersions.getLast();
    }

    private byte[] manifest(
            DocumentExportRequest exportRequest,
            DocumentRecord document,
            DocumentVersionRecord version) {
        String body = "{\n"
                + "  \"notice\": \"Sandbox governed export; not an official land title or certificate.\",\n"
                + "  \"exportRequestId\": \"" + exportRequest.id() + "\",\n"
                + "  \"documentId\": \"" + document.id() + "\",\n"
                + "  \"documentVersionId\": \"" + version.id() + "\",\n"
                + "  \"classification\": \"" + document.classification().name() + "\",\n"
                + "  \"redactionPlanned\": " + exportRequest.redactionPlanned() + ",\n"
                + "  \"sourceChecksumSha256\": \"" + version.checksumSha256() + "\"\n"
                + "}\n";
        return body.getBytes(StandardCharsets.UTF_8);
    }

    private byte[] packageContent(byte[] manifest, byte[] sourceContent) {
        byte[] separator = "\n---SOURCE-DOCUMENT---\n".getBytes(StandardCharsets.UTF_8);
        byte[] packageContent = new byte[manifest.length + separator.length + sourceContent.length];
        System.arraycopy(manifest, 0, packageContent, 0, manifest.length);
        System.arraycopy(separator, 0, packageContent, manifest.length, separator.length);
        System.arraycopy(sourceContent, 0, packageContent, manifest.length + separator.length, sourceContent.length);
        return packageContent;
    }

    private String newToken() {
        byte[] token = new byte[32];
        TOKEN_RANDOM.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }

    private String sha256(byte[] value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
