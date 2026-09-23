package cd.edrc.landgis.parties;

import java.util.UUID;

public class PartyNotFoundException extends RuntimeException {
    public PartyNotFoundException(UUID partyId) {
        super("Party not found: " + partyId);
    }
}
