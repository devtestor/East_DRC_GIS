package cd.edrc.landgis.workflow;

import java.util.UUID;

public class UnsupportedWorkflowTaskException extends RuntimeException {
    public UnsupportedWorkflowTaskException(UUID taskId) {
        super("Workflow task is not supported by this operation: " + taskId);
    }
}
