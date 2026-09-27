package cd.edrc.landgis.governance;

import java.util.UUID;

public class DocumentExportRequestNotFoundException extends RuntimeException {
    public DocumentExportRequestNotFoundException(UUID requestId) {
        super("Document export request not found: " + requestId);
    }
}
