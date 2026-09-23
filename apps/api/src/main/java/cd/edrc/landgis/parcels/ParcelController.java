package cd.edrc.landgis.parcels;

import jakarta.validation.Valid;
import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parcels")
class ParcelController {
    private final ParcelRegistryService parcelRegistry;
    private final ParcelGeometryService parcelGeometries;
    private final AuthenticatedActorResolver actors;

    ParcelController(
            ParcelRegistryService parcelRegistry,
            ParcelGeometryService parcelGeometries,
            AuthenticatedActorResolver actors) {
        this.parcelRegistry = parcelRegistry;
        this.parcelGeometries = parcelGeometries;
        this.actors = actors;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ParcelResponse createDraft(@Valid @RequestBody CreateParcelRequest request) {
        return parcelRegistry.createDraft(request);
    }

    @GetMapping
    List<ParcelResponse> search(@RequestParam UUID administrativeUnitId) {
        return parcelRegistry.findByAdministrativeUnit(administrativeUnitId);
    }

    @GetMapping("/{parcelId}")
    ParcelResponse getParcel(@PathVariable UUID parcelId) {
        return parcelRegistry.getParcel(parcelId);
    }

    @GetMapping("/search")
    Optional<ParcelResponse> searchByProposedUpi(@RequestParam String upi) {
        return parcelRegistry.findByProposedUpi(upi);
    }

    @PatchMapping("/{parcelId}/status")
    ParcelTransitionResponse transitionStatus(
            @PathVariable UUID parcelId,
            @Valid @RequestBody TransitionParcelStatusRequest request,
            Principal principal) {
        return parcelRegistry.transitionStatus(parcelId, request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{parcelId}/geometries")
    @ResponseStatus(HttpStatus.CREATED)
    ParcelGeometryVersionResponse createDraftGeometry(
            @PathVariable UUID parcelId,
            @Valid @RequestBody CreateParcelGeometryVersionRequest request) {
        return parcelGeometries.createDraftGeometry(parcelId, request);
    }

    @GetMapping("/{parcelId}/geometries")
    List<ParcelGeometryVersionResponse> listGeometryVersions(@PathVariable UUID parcelId) {
        return parcelGeometries.listGeometryVersions(parcelId);
    }

    @PostMapping("/{parcelId}/geometries/{geometryVersionId}/approval-requests")
    ParcelGeometryApprovalResponse requestGeometryApproval(
            @PathVariable UUID parcelId,
            @PathVariable UUID geometryVersionId,
            Principal principal) {
        return parcelGeometries.requestApproval(
                parcelId,
                geometryVersionId,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/geometries/tasks/{taskId}/decisions")
    ParcelGeometryApprovalResponse decideGeometryApproval(
            @PathVariable UUID taskId,
            @Valid @RequestBody DecideWorkflowTaskRequest request,
            Principal principal) {
        return parcelGeometries.decideApprovalTask(
                taskId,
                request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }
}
