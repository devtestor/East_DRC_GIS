package cd.edrc.landgis.governance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateDocumentExportRequest(
        @NotNull UUID documentId,
        @NotBlank @Size(max = 500) String purpose,
        boolean redactionPlanned) {
}
