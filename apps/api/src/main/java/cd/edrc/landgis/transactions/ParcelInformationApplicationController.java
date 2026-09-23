package cd.edrc.landgis.transactions;

import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/applications")
class ParcelInformationApplicationController {
    private final ParcelInformationApplicationService applications;
    private final AuthenticatedActorResolver actors;

    ParcelInformationApplicationController(ParcelInformationApplicationService applications, AuthenticatedActorResolver actors) {
        this.applications = applications;
        this.actors = actors;
    }

    @PostMapping("/parcel-information-requests")
    @ResponseStatus(HttpStatus.CREATED)
    ParcelInformationApplicationResponse create(
            @Valid @RequestBody CreateParcelInformationApplicationRequest request,
            Principal principal) {
        return applications.create(request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping("/{applicationId}")
    ParcelInformationApplicationResponse get(@PathVariable UUID applicationId, Principal principal) {
        return applications.get(applicationId, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping
    List<ParcelInformationApplicationResponse> listMine(Principal principal) {
        return applications.listMine(actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{applicationId}/correction-requests")
    ParcelInformationApplicationResponse requestCorrection(
            @PathVariable UUID applicationId,
            @Valid @RequestBody CorrectionRequest request,
            Principal principal) {
        return applications.requestCorrection(applicationId, request.reason(),
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{applicationId}/payments/sandbox-confirmation")
    ParcelInformationApplicationResponse confirmPayment(
            @PathVariable UUID applicationId,
            @Valid @RequestBody PaymentConfirmationRequest request,
            Principal principal) {
        return applications.confirmSandboxPayment(applicationId, request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{applicationId}/review-requests")
    ParcelInformationReviewResponse requestReview(@PathVariable UUID applicationId, Principal principal) {
        return applications.requestReview(applicationId,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/tasks/{taskId}/decisions")
    ParcelInformationReviewResponse decideReview(
            @PathVariable UUID taskId,
            @Valid @RequestBody DecideWorkflowTaskRequest request,
            Principal principal) {
        return applications.decideReviewTask(taskId, request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }
}
