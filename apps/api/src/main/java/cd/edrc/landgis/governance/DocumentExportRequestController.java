package cd.edrc.landgis.governance;

import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/governance/export-requests")
class DocumentExportRequestController {
    private final DocumentExportRequestService exportRequests;
    private final AuthenticatedActorResolver actors;

    DocumentExportRequestController(DocumentExportRequestService exportRequests, AuthenticatedActorResolver actors) {
        this.exportRequests = exportRequests;
        this.actors = actors;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DocumentExportRequestResponse create(
            @Valid @RequestBody CreateDocumentExportRequest request,
            Principal principal) {
        return exportRequests.create(request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping("/{requestId}")
    DocumentExportRequestResponse get(@PathVariable UUID requestId) {
        return exportRequests.get(requestId);
    }

    @PostMapping("/{requestId}/review-completion")
    DocumentExportRequestResponse completeReview(
            @PathVariable UUID requestId,
            @Valid @RequestBody CompleteDocumentExportReviewRequest request,
            Principal principal) {
        return exportRequests.completeReview(
                requestId,
                request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }
}
