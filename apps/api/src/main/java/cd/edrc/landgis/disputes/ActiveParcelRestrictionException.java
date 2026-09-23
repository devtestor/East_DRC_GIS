package cd.edrc.landgis.disputes;

import java.util.UUID;

public class ActiveParcelRestrictionException extends RuntimeException {
    public ActiveParcelRestrictionException(UUID parcelId) {
        super("Parcel has an active restriction blocking ownership-interest changes: " + parcelId);
    }
}
