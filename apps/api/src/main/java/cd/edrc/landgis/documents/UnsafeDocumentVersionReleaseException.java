package cd.edrc.landgis.documents;

import java.util.UUID;

public class UnsafeDocumentVersionReleaseException extends RuntimeException {
    public UnsafeDocumentVersionReleaseException(UUID documentId, UUID versionId) {
        super("Document version cannot be released while malware scan or signature status is unsafe: "
                + versionId + " for document " + documentId);
    }
}
