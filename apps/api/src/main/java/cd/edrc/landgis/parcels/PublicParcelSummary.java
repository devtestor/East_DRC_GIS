package cd.edrc.landgis.parcels;

import java.util.UUID;

record PublicParcelSummary(
        UUID parcelId,
        String status,
        String proposedUpi,
        UUID administrativeUnitId,
        String administrativeUnitName,
        String administrativeUnitType,
        String landUse,
        String tenureClassification) {
}
