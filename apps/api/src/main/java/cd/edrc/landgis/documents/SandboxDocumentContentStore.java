package cd.edrc.landgis.documents;

import java.nio.charset.StandardCharsets;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Local-only content store. Production must bind this contract to encrypted object storage. */
@Component
@Profile("!production")
class SandboxDocumentContentStore implements DocumentContentStore {
    @Override
    public byte[] read(DocumentRecord document, DocumentVersionRecord version) {
        String body = "EDRC Land Services - Operational report\n"
                + "Document: " + document.id() + "\n"
                + "Title: " + document.title() + "\n"
                + "Classification: " + document.classification().name() + "\n"
                + "This sandbox output is not an official land title or government certificate.\n";
        return body.getBytes(StandardCharsets.UTF_8);
    }
}
