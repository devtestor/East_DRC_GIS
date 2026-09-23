package cd.edrc.landgis.workflow;

import java.util.UUID;

public class MakerCheckerViolationException extends RuntimeException {
    public MakerCheckerViolationException(UUID taskId, String actor) {
        super("Maker-checker separation prevents actor " + actor + " from approving workflow task " + taskId);
    }
}
