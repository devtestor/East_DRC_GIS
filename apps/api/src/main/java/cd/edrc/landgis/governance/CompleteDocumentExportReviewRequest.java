package cd.edrc.landgis.governance;

import cd.edrc.landgis.workflow.WorkflowDecision;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompleteDocumentExportReviewRequest(
        @NotNull WorkflowDecision decision,
        @NotBlank @Size(max = 500) String reason) {
}
