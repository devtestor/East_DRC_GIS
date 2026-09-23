package cd.edrc.landgis.workflow;

import java.util.UUID;

public class WorkflowTaskAssignmentRequiredException extends RuntimeException {
    public WorkflowTaskAssignmentRequiredException(UUID taskId) {
        super("Workflow task must be claimed before it can be decided: " + taskId);
    }
}
