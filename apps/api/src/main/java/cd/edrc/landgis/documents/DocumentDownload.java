package cd.edrc.landgis.documents;

public record DocumentDownload(byte[] content, String filename, String mediaType) {
}
