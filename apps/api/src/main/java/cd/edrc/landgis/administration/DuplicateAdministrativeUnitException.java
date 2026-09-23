package cd.edrc.landgis.administration;

class DuplicateAdministrativeUnitException extends RuntimeException {
    DuplicateAdministrativeUnitException(AdministrativeUnitType unitType, String code) {
        super("Administrative unit already exists for " + unitType + " and code " + code);
    }
}
