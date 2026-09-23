package cd.edrc.landgis.workflow;

import java.util.UUID;

public class WorkflowTaskNotOpenException extends RuntimeException {
    public WorkflowTaskNotOpenException(UUID taskId, WorkflowTaskStatus status) {
        super("Workflow task " + taskId + " is not open; current status is " + status);
    }
}
