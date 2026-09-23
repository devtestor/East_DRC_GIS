package cd.edrc.landgis.parcels;

class DuplicateUpiException extends RuntimeException {
    DuplicateUpiException(String upi) {
        super("Active proposed UPI already exists: " + upi);
    }
}
