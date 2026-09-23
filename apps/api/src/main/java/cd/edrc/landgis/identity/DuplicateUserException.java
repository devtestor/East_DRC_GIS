package cd.edrc.landgis.identity;

class DuplicateUserException extends RuntimeException {
    DuplicateUserException(String email) {
        super("A user with this email already exists: " + email);
    }
}
