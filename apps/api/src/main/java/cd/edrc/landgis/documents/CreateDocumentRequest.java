package cd.edrc.landgis.documents;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateDocumentRequest(
        @NotBlank @Size(max = 80) String documentType,
        @NotBlank @Size(max = 80) String ownerType,
        @NotNull UUID ownerId,
        @NotBlank @Size(max = 200) String title,
        @NotNull DocumentClassification classification,
        @NotBlank @Size(max = 80) String retentionCategory,
        @NotBlank @Size(max = 120) String accessPolicy,
        UUID custodianOrganizationId,
        @Size(max = 80) String custodianRoleCode,
        boolean legalHold,
        OffsetDateTime effectiveAt,
        @NotBlank @Size(max = 300) String objectStorageKey,
        @NotBlank @Size(max = 255) String originalFilename,
        @NotBlank @Size(max = 120) String mediaType,
        @Min(0) long sizeBytes,
        @NotBlank @Pattern(regexp = "^[A-Fa-f0-9]{64}$") String checksumSha256,
        @NotNull MalwareScanStatus malwareScanStatus,
        @NotNull DigitalSignatureStatus digitalSignatureStatus) {
}
