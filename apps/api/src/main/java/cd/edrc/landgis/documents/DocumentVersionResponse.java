package cd.edrc.landgis.documents;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DocumentVersionResponse(
        UUID id,
        UUID documentId,
        int versionNumber,
        String objectStorageKey,
        String originalFilename,
        String mediaType,
        long sizeBytes,
        String checksumSha256,
        String malwareScanStatus,
        String digitalSignatureStatus,
        String safetyStatus,
        String safetyReason,
        UUID safetyReviewedByUserId,
        String safetyReviewedBy,
        OffsetDateTime safetyReviewedAt,
        UUID uploadedByUserId,
        String uploadedBy,
        OffsetDateTime uploadedAt) {
    static DocumentVersionResponse from(DocumentVersionRecord version) {
        return new DocumentVersionResponse(
                version.id(),
                version.documentId(),
                version.versionNumber(),
                version.objectStorageKey(),
                version.originalFilename(),
                version.mediaType(),
                version.sizeBytes(),
                version.checksumSha256(),
                version.malwareScanStatus().name(),
                version.digitalSignatureStatus().name(),
                version.safetyStatus().name(),
                version.safetyReason(),
                version.safetyReviewedByUserId(),
                version.safetyReviewedBy(),
                version.safetyReviewedAt(),
                version.uploadedByUserId(),
                version.uploadedBy(),
                version.uploadedAt());
    }
}
