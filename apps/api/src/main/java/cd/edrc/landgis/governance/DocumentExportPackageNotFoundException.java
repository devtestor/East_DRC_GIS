package cd.edrc.landgis.governance;

import java.util.UUID;

public class DocumentExportPackageNotFoundException extends RuntimeException {
    public DocumentExportPackageNotFoundException(UUID packageId) {
        super("Document export package not found: " + packageId);
    }
}
