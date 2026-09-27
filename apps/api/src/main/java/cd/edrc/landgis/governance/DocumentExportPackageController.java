package cd.edrc.landgis.governance;

import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
@RequestMapping("/api/v1/governance")
class DocumentExportPackageController {
    private final DocumentExportPackageService exportPackages;
    private final AuthenticatedActorResolver actors;

    DocumentExportPackageController(DocumentExportPackageService exportPackages, AuthenticatedActorResolver actors) {
        this.exportPackages = exportPackages;
        this.actors = actors;
    }

    @PostMapping("/export-requests/{requestId}/packages")
    @ResponseStatus(HttpStatus.CREATED)
    DocumentExportPackageResponse generate(
            @PathVariable UUID requestId,
            @Valid @RequestBody CreateDocumentExportPackageRequest request,
            Principal principal) {
        return exportPackages.generate(
                requestId,
                request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping("/export-packages/{packageId}")
    DocumentExportPackageResponse get(@PathVariable UUID packageId) {
        return exportPackages.get(packageId);
    }

    @GetMapping("/export-packages/{packageId}/download")
    ResponseEntity<byte[]> download(
            @PathVariable UUID packageId,
            @RequestParam("token") String token,
            Principal principal) {
        DocumentExportPackageDownload download = exportPackages.download(
                packageId,
                token,
                actors.requireActor(principal == null ? null : principal.getName()));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(download.mediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.filename())
                        .build()
                        .toString())
                .body(download.content());
    }
}
