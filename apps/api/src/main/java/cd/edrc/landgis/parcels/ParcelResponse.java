package cd.edrc.landgis.parcels;

import java.util.UUID;

record ParcelResponse(UUID id, String status, UUID administrativeUnitId, String proposedUpi) {
}
