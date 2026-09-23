package cd.edrc.landgis.pilot;

import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pilots")
class PilotReadinessController {
    private final PilotReadinessService pilots;
    private final AuthenticatedActorResolver actors;

    PilotReadinessController(PilotReadinessService pilots, AuthenticatedActorResolver actors) {
        this.pilots = pilots;
        this.actors = actors;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PilotReadinessResponse create(@Valid @RequestBody CreatePilotReadinessRequest request, Principal principal) {
        return pilots.create(request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping
    List<PilotReadinessResponse> list() {
        return pilots.list();
    }

    @GetMapping("/{pilotId}")
    PilotReadinessResponse get(@PathVariable UUID pilotId) {
        return pilots.get(pilotId);
    }

    @PostMapping("/{pilotId}/signoffs")
    @ResponseStatus(HttpStatus.CREATED)
    PilotSignoffResponse addSignoff(
            @PathVariable UUID pilotId,
            @Valid @RequestBody CreatePilotSignoffRequest request,
            Principal principal) {
        return pilots.addSignoff(pilotId, request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/signoffs/tasks/{taskId}/decisions")
    PilotSignoffResponse decideSignoffTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody DecideWorkflowTaskRequest request,
            Principal principal) {
        return pilots.decideSignoffTask(
                taskId,
                request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{pilotId}/risks")
    @ResponseStatus(HttpStatus.CREATED)
    PilotRiskResponse addRisk(
            @PathVariable UUID pilotId,
            @Valid @RequestBody CreatePilotRiskRequest request,
            Principal principal) {
        return pilots.addRisk(pilotId, request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PatchMapping("/{pilotId}/risks/{riskId}")
    PilotRiskResponse updateRiskStatus(
            @PathVariable UUID pilotId,
            @PathVariable UUID riskId,
            @Valid @RequestBody UpdatePilotRiskStatusRequest request,
            Principal principal) {
        return pilots.updateRiskStatus(
                pilotId,
                riskId,
                request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{pilotId}/evidence")
    @ResponseStatus(HttpStatus.CREATED)
    PilotEvidenceResponse addEvidence(
            @PathVariable UUID pilotId,
            @Valid @RequestBody CreatePilotEvidenceRequest request,
            Principal principal) {
        return pilots.addEvidence(pilotId, request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{pilotId}/go-no-go-requests")
    PilotWorkflowResponse requestGoNoGo(@PathVariable UUID pilotId, Principal principal) {
        return pilots.requestGoNoGo(pilotId, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/tasks/{taskId}/decisions")
    PilotWorkflowResponse decideGoNoGoTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody DecideWorkflowTaskRequest request,
            Principal principal) {
        return pilots.decideGoNoGoTask(
                taskId,
                request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }
}
