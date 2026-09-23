package cd.edrc.landgis.parcels;

import java.util.UUID;

public class InvalidParcelGeometryStateException extends RuntimeException {
    public InvalidParcelGeometryStateException(UUID geometryVersionId, String status) {
        super("Parcel geometry version cannot enter the requested workflow from status " + status + ": " + geometryVersionId);
    }
}
