package cd.edrc.landgis.documents;

import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/documents")
class DocumentController {
    private final DocumentService documents;
    private final AuthenticatedActorResolver actors;

    DocumentController(DocumentService documents, AuthenticatedActorResolver actors) {
        this.documents = documents;
        this.actors = actors;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DocumentResponse create(@Valid @RequestBody CreateDocumentRequest request, Principal principal) {
        return documents.create(request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping("/{documentId}")
    DocumentResponse get(@PathVariable UUID documentId, Principal principal) {
        return documents.get(documentId, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping("/{documentId}/content")
    ResponseEntity<byte[]> download(@PathVariable UUID documentId, Principal principal) {
        DocumentDownload download = documents.download(
                documentId, actors.requireActor(principal == null ? null : principal.getName()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(download.mediaType()));
        headers.setContentDisposition(ContentDisposition.attachment().filename(download.filename()).build());
        return new ResponseEntity<>(download.content(), headers, HttpStatus.OK);
    }

    @PostMapping("/{documentId}/versions")
    DocumentResponse addVersion(
            @PathVariable UUID documentId,
            @Valid @RequestBody AddDocumentVersionRequest request,
            Principal principal) {
        return documents.addVersion(
                documentId,
                request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping
    List<DocumentResponse> findByOwner(@RequestParam String ownerType, @RequestParam UUID ownerId, Principal principal) {
        return documents.findByOwner(
                ownerType,
                ownerId,
                actors.requireActor(principal == null ? null : principal.getName()));
    }
}
