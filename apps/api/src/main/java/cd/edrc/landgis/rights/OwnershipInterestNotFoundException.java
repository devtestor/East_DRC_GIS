package cd.edrc.landgis.rights;

import java.util.UUID;

public class OwnershipInterestNotFoundException extends RuntimeException {
    public OwnershipInterestNotFoundException(UUID interestId) {
        super("Ownership interest record not found: " + interestId);
    }
}
