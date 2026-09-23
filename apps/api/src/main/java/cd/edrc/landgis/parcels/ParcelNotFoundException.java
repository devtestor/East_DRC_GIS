package cd.edrc.landgis.parcels;

import java.util.UUID;

public class ParcelNotFoundException extends RuntimeException {
    public ParcelNotFoundException(UUID parcelId) {
        super("Parcel does not exist: " + parcelId);
    }
}
