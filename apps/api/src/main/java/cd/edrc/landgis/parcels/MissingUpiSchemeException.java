package cd.edrc.landgis.parcels;

class MissingUpiSchemeException extends RuntimeException {
    MissingUpiSchemeException() {
        super("No active proposed UPI scheme is configured");
    }
}
