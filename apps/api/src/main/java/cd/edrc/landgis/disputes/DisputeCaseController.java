package cd.edrc.landgis.disputes;

import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
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
@RequestMapping("/api/v1/parcels/{parcelId}/disputes")
class DisputeCaseController {
    private final DisputeCaseService disputes;
    private final AuthenticatedActorResolver actors;

    DisputeCaseController(DisputeCaseService disputes, AuthenticatedActorResolver actors) {
        this.disputes = disputes;
        this.actors = actors;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    DisputeCaseResponse create(
            @PathVariable UUID parcelId,
            @Valid @RequestBody CreateDisputeCaseRequest request,
            Principal principal) {
        return disputes.create(parcelId, request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping
    List<DisputeCaseResponse> list(@PathVariable UUID parcelId) {
        return disputes.listByParcel(parcelId);
    }

    @PostMapping("/{caseId}/hearings")
    DisputeHearingResponse scheduleHearing(
            @PathVariable UUID parcelId,
            @PathVariable UUID caseId,
            @Valid @RequestBody ScheduleDisputeHearingRequest request,
            Principal principal) {
        return disputes.scheduleHearing(parcelId, caseId, request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping("/{caseId}/hearings")
    List<DisputeHearingResponse> listHearings(@PathVariable UUID parcelId, @PathVariable UUID caseId) {
        return disputes.listHearings(parcelId, caseId);
    }

    @PostMapping("/{caseId}/decision-requests")
    DisputeDecisionResponse requestDecision(
            @PathVariable UUID parcelId, @PathVariable UUID caseId, Principal principal) {
        return disputes.requestDecision(parcelId, caseId,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/tasks/{taskId}/decision-decisions")
    DisputeDecisionResponse decideDecision(
            @PathVariable UUID parcelId,
            @PathVariable UUID taskId,
            @Valid @RequestBody DecideWorkflowTaskRequest request,
            Principal principal) {
        return disputes.decideDecisionTask(parcelId, taskId, request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{caseId}/reopen-requests")
    DisputeDecisionResponse requestReopen(
            @PathVariable UUID parcelId, @PathVariable UUID caseId, Principal principal) {
        return disputes.requestReopen(parcelId, caseId,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/tasks/{taskId}/reopen-decisions")
    DisputeDecisionResponse decideReopen(
            @PathVariable UUID parcelId,
            @PathVariable UUID taskId,
            @Valid @RequestBody DecideWorkflowTaskRequest request,
            Principal principal) {
        return disputes.decideReopenTask(parcelId, taskId, request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{caseId}/appeals")
    DisputeAppealResponse createAppeal(
            @PathVariable UUID parcelId,
            @PathVariable UUID caseId,
            @Valid @RequestBody CreateDisputeAppealRequest request,
            Principal principal) {
        return disputes.createAppeal(parcelId, caseId, request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping("/{caseId}/appeals")
    List<DisputeAppealResponse> listAppeals(@PathVariable UUID parcelId, @PathVariable UUID caseId) {
        return disputes.listAppeals(parcelId, caseId);
    }

    @PostMapping("/{caseId}/documents")
    DisputeDocumentResponse linkDocument(
            @PathVariable UUID parcelId,
            @PathVariable UUID caseId,
            @Valid @RequestBody LinkDisputeDocumentRequest request,
            Principal principal) {
        return disputes.linkDocument(parcelId, caseId, request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping("/{caseId}/documents")
    List<DisputeDocumentResponse> listDocuments(
            @PathVariable UUID parcelId,
            @PathVariable UUID caseId,
            Principal principal) {
        return disputes.listDocuments(parcelId, caseId,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{caseId}/review-requests")
    DisputeCaseReviewResponse requestReview(
            @PathVariable UUID parcelId,
            @PathVariable UUID caseId,
            Principal principal) {
        return disputes.requestReview(parcelId, caseId, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/tasks/{taskId}/decisions")
    DisputeCaseReviewResponse decideReviewTask(
            @PathVariable UUID parcelId,
            @PathVariable UUID taskId,
            @Valid @RequestBody DecideWorkflowTaskRequest request,
            Principal principal) {
        return disputes.decideReviewTask(parcelId, taskId, request, actors.requireActor(principal == null ? null : principal.getName()));
    }
}
