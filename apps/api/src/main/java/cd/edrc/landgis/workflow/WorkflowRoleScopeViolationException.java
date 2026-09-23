package cd.edrc.landgis.workflow;

import java.util.UUID;

public class WorkflowRoleScopeViolationException extends RuntimeException {
    public WorkflowRoleScopeViolationException(UUID userId, String requiredRole) {
        super("User " + userId + " does not have active workflow role scope: " + requiredRole);
    }
}
