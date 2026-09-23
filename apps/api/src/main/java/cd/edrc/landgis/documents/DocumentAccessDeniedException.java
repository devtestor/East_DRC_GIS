package cd.edrc.landgis.documents;

import java.util.UUID;

public class DocumentAccessDeniedException extends RuntimeException {
    public DocumentAccessDeniedException(UUID documentId) {
        super("Access denied for document: " + documentId);
    }
}
