package cd.edrc.landgis.documents;

import java.util.UUID;

public class DocumentVersionNotFoundException extends RuntimeException {
    public DocumentVersionNotFoundException(UUID documentId, UUID versionId) {
        super("Document version not found: " + versionId + " for document " + documentId);
    }
}
