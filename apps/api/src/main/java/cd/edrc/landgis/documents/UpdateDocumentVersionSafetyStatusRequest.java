package cd.edrc.landgis.documents;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDocumentVersionSafetyStatusRequest(
        @NotNull MalwareScanStatus malwareScanStatus,
        @NotNull DigitalSignatureStatus digitalSignatureStatus,
        @NotBlank @Size(max = 500) String reason) {
}
