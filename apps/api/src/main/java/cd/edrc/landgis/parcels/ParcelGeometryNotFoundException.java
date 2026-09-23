package cd.edrc.landgis.parcels;

import java.util.UUID;

public class ParcelGeometryNotFoundException extends RuntimeException {
    public ParcelGeometryNotFoundException(UUID geometryVersionId) {
        super("Parcel geometry version not found: " + geometryVersionId);
    }
}
