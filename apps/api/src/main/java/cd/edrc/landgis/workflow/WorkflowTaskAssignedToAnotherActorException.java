package cd.edrc.landgis.workflow;

import java.util.UUID;

public class WorkflowTaskAssignedToAnotherActorException extends RuntimeException {
    public WorkflowTaskAssignedToAnotherActorException(UUID taskId, String assignedToActor, String attemptedActor) {
        super("Workflow task " + taskId + " is assigned to " + assignedToActor + ", not " + attemptedActor);
    }
}
