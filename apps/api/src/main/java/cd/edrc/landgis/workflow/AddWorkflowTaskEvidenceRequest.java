package cd.edrc.landgis.workflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record AddWorkflowTaskEvidenceRequest(
        @NotNull WorkflowEvidenceType evidenceType,
        @NotBlank @Size(max = 80) String referenceType,
        UUID referenceId,
        @Size(max = 200) String externalReference,
        @NotBlank @Size(max = 500) String summary) {
}
