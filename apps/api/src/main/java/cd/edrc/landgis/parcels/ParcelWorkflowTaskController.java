package cd.edrc.landgis.parcels;

import cd.edrc.landgis.workflow.AddWorkflowTaskEvidenceRequest;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import cd.edrc.landgis.workflow.WorkflowTaskEvidenceResponse;
import cd.edrc.landgis.workflow.WorkflowTaskResponse;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/workflow/tasks")
class ParcelWorkflowTaskController {
    private final WorkflowTaskService workflowTasks;
    private final ParcelRegistryService parcelRegistry;
    private final AuthenticatedActorResolver actors;

    ParcelWorkflowTaskController(
            WorkflowTaskService workflowTasks,
            ParcelRegistryService parcelRegistry,
            AuthenticatedActorResolver actors) {
        this.workflowTasks = workflowTasks;
        this.parcelRegistry = parcelRegistry;
        this.actors = actors;
    }

    @GetMapping
    List<WorkflowTaskResponse> listOpenTasks() {
        return workflowTasks.listOpenTasks();
    }

    @PostMapping("/{taskId}/claim")
    WorkflowTaskResponse claimTask(@PathVariable UUID taskId, Principal principal) {
        return workflowTasks.claimTask(taskId, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping("/{taskId}/evidence")
    List<WorkflowTaskEvidenceResponse> listEvidence(@PathVariable UUID taskId) {
        return workflowTasks.listEvidence(taskId);
    }

    @PostMapping("/{taskId}/evidence")
    WorkflowTaskEvidenceResponse addEvidence(
            @PathVariable UUID taskId,
            @Valid @RequestBody AddWorkflowTaskEvidenceRequest request,
            Principal principal) {
        return workflowTasks.addEvidence(
                taskId,
                request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{taskId}/decisions")
    ParcelTransitionResponse decideParcelTransitionTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody DecideWorkflowTaskRequest request,
            Principal principal) {
        return parcelRegistry.decideStatusTransitionTask(
                taskId,
                request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }
}
