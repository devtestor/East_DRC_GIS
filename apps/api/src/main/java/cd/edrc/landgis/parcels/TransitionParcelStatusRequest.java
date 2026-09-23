package cd.edrc.landgis.parcels;

import jakarta.validation.constraints.NotNull;

record TransitionParcelStatusRequest(@NotNull ParcelStatus targetStatus) {
}
