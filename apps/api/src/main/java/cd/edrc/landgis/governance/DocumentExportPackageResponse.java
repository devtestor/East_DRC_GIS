package cd.edrc.landgis.governance;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentExportPackageResponse(
        UUID id,
        UUID exportRequestId,
        UUID documentId,
        UUID documentVersionId,
        String objectStorageKey,
        String manifestSha256,
        String packageSha256,
        long packageSizeBytes,
        OffsetDateTime expiresAt,
        UUID generatedByUserId,
        String generatedBy,
        OffsetDateTime generatedAt,
        OffsetDateTime downloadedAt,
        int downloadCount,
        String deliveryToken) {
    static DocumentExportPackageResponse from(DocumentExportPackage exportPackage, String deliveryToken) {
        return new DocumentExportPackageResponse(
                exportPackage.id(),
                exportPackage.exportRequestId(),
                exportPackage.documentId(),
                exportPackage.documentVersionId(),
                exportPackage.objectStorageKey(),
                exportPackage.manifestSha256(),
                exportPackage.packageSha256(),
                exportPackage.packageSizeBytes(),
                exportPackage.expiresAt(),
                exportPackage.generatedByUserId(),
                exportPackage.generatedBy(),
                exportPackage.generatedAt(),
                exportPackage.downloadedAt(),
                exportPackage.downloadCount(),
                deliveryToken);
    }
}
