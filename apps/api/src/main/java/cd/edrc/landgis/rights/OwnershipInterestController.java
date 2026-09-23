package cd.edrc.landgis.rights;

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
@RequestMapping("/api/v1/parcels/{parcelId}/ownership-interests")
class OwnershipInterestController {
    private final OwnershipInterestService interests;
    private final AuthenticatedActorResolver actors;

    OwnershipInterestController(OwnershipInterestService interests, AuthenticatedActorResolver actors) {
        this.interests = interests;
        this.actors = actors;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    OwnershipInterestResponse create(
            @PathVariable UUID parcelId,
            @Valid @RequestBody CreateOwnershipInterestRequest request,
            Principal principal) {
        return interests.create(parcelId, request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping
    List<OwnershipInterestResponse> list(@PathVariable UUID parcelId) {
        return interests.listByParcel(parcelId);
    }

    @GetMapping("/conflicts")
    List<OwnershipConflictResponse> listConflicts(@PathVariable UUID parcelId) {
        return interests.listConflicts(parcelId);
    }

    @PostMapping("/{interestId}/review-requests")
    OwnershipInterestReviewResponse requestReview(
            @PathVariable UUID parcelId,
            @PathVariable UUID interestId,
            Principal principal) {
        return interests.requestReview(
                parcelId,
                interestId,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/tasks/{taskId}/decisions")
    OwnershipInterestReviewResponse decideReviewTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody DecideWorkflowTaskRequest request,
            Principal principal) {
        return interests.decideReviewTask(
                taskId,
                request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }
}
