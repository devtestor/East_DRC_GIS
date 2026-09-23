package cd.edrc.landgis.parcels;

class InvalidParcelStateTransitionException extends RuntimeException {
    InvalidParcelStateTransitionException(ParcelStatus fromStatus, ParcelStatus toStatus) {
        super("Parcel cannot transition from " + fromStatus + " to " + toStatus);
    }
}
