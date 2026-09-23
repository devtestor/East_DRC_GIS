package cd.edrc.landgis.workflow;

import java.util.UUID;

public class WorkflowEvidenceRequiredException extends RuntimeException {
    public WorkflowEvidenceRequiredException(UUID taskId) {
        super("Workflow task approval requires at least one evidence link: " + taskId);
    }
}
