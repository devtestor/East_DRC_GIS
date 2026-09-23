package cd.edrc.landgis.parcels;

import java.util.UUID;

class UnknownAdministrativeUnitException extends RuntimeException {
    UnknownAdministrativeUnitException(UUID id) {
        super("Administrative unit does not exist: " + id);
    }
}
