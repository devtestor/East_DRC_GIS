package cd.edrc.landgis.workflow;

import java.util.UUID;

public class WorkflowTaskNotFoundException extends RuntimeException {
    public WorkflowTaskNotFoundException(UUID taskId) {
        super("Workflow task not found: " + taskId);
    }
}
