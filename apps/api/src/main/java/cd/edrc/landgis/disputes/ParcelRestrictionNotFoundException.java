package cd.edrc.landgis.disputes;

import java.util.UUID;

public class ParcelRestrictionNotFoundException extends RuntimeException {
    public ParcelRestrictionNotFoundException(UUID restrictionId) {
        super("Parcel restriction not found: " + restrictionId);
    }
}
