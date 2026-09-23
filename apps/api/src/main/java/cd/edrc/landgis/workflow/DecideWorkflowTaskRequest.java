package cd.edrc.landgis.workflow;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DecideWorkflowTaskRequest(
        @NotNull WorkflowDecision decision,
        @NotBlank @Size(max = 500) String reason) {
}
