package cd.edrc.landgis.documents;

public interface DocumentContentStore {
    byte[] read(DocumentRecord document, DocumentVersionRecord version);
}
