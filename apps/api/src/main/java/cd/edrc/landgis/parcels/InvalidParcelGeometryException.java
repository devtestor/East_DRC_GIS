package cd.edrc.landgis.parcels;

import java.util.UUID;

public class InvalidParcelGeometryException extends RuntimeException {
    public InvalidParcelGeometryException(UUID parcelId) {
        super("Parcel geometry is invalid or cannot be stored for parcel: " + parcelId);
    }
}
