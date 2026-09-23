package cd.edrc.landgis.administration;

import java.util.UUID;

class UnknownParentAdministrativeUnitException extends RuntimeException {
    UnknownParentAdministrativeUnitException(UUID parentId) {
        super("Parent administrative unit does not exist: " + parentId);
    }
}
