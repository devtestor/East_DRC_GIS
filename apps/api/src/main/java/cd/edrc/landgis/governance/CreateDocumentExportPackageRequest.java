package cd.edrc.landgis.governance;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CreateDocumentExportPackageRequest(
        @Min(5)
        @Max(1440)
        int expiresInMinutes) {
}
