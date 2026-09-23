package cd.edrc.landgis.documents;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddDocumentVersionRequest(
        @NotBlank @Size(max = 300) String objectStorageKey,
        @NotBlank @Size(max = 255) String originalFilename,
        @NotBlank @Size(max = 120) String mediaType,
        @Min(0) long sizeBytes,
        @NotBlank @Pattern(regexp = "^[A-Fa-f0-9]{64}$") String checksumSha256,
        @NotNull MalwareScanStatus malwareScanStatus,
        @NotNull DigitalSignatureStatus digitalSignatureStatus) {
}
