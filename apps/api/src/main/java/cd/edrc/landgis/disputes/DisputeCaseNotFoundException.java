package cd.edrc.landgis.disputes;

import java.util.UUID;

public class DisputeCaseNotFoundException extends RuntimeException {
    public DisputeCaseNotFoundException(UUID caseId) {
        super("Dispute case not found: " + caseId);
    }
}
