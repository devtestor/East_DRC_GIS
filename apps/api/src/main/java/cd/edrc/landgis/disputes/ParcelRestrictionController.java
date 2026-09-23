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
@RequestMapping("/api/v1/parcels/{parcelId}/restrictions")
class ParcelRestrictionController {
    private final ParcelRestrictionService restrictions;
    private final AuthenticatedActorResolver actors;

    ParcelRestrictionController(ParcelRestrictionService restrictions, AuthenticatedActorResolver actors) {
        this.restrictions = restrictions;
        this.actors = actors;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ParcelRestrictionResponse create(
            @PathVariable UUID parcelId,
            @Valid @RequestBody CreateParcelRestrictionRequest request,
            Principal principal) {
        return restrictions.create(parcelId, request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping
    List<ParcelRestrictionResponse> list(@PathVariable UUID parcelId) {
        return restrictions.listByParcel(parcelId);
    }

    @PostMapping("/{restrictionId}/release-requests")
    ParcelRestrictionReleaseResponse requestRelease(
            @PathVariable UUID parcelId,
            @PathVariable UUID restrictionId,
            Principal principal) {
        return restrictions.requestRelease(
                parcelId,
                restrictionId,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/tasks/{taskId}/decisions")
    ParcelRestrictionReleaseResponse decideReleaseTask(
            @PathVariable UUID parcelId,
            @PathVariable UUID taskId,
            @Valid @RequestBody DecideWorkflowTaskRequest request,
            Principal principal) {
        return restrictions.decideReleaseTask(
                parcelId,
                taskId,
                request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }
}
