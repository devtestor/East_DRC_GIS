package cd.edrc.landgis.disputes;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record LinkDisputeDocumentRequest(
        @NotNull UUID documentId,
        @NotNull DisputeDocumentRelationship relationship,
        @NotBlank @Size(max = 1000) String summary) {
}
