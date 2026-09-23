package cd.edrc.landgis.documents;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        String documentType,
        String ownerType,
        UUID ownerId,
        String title,
        String classification,
        String retentionCategory,
        String accessPolicy,
        UUID custodianOrganizationId,
        String custodianRoleCode,
        boolean legalHold,
        UUID createdByUserId,
        String createdBy,
        OffsetDateTime createdAt,
        OffsetDateTime effectiveAt,
        List<DocumentVersionResponse> versions) {
    static DocumentResponse from(DocumentRecord document, List<DocumentVersionRecord> versions) {
        return new DocumentResponse(
                document.id(),
                document.documentType(),
                document.ownerType(),
                document.ownerId(),
                document.title(),
                document.classification().name(),
                document.retentionCategory(),
                document.accessPolicy(),
                document.custodianOrganizationId(),
                document.custodianRoleCode(),
                document.legalHold(),
                document.createdByUserId(),
                document.createdBy(),
                document.createdAt(),
                document.effectiveAt(),
                versions.stream().map(DocumentVersionResponse::from).toList());
    }
}
