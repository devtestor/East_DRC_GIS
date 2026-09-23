package cd.edrc.landgis.audit;

import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import cd.edrc.landgis.workflow.UnsupportedWorkflowTaskException;
import cd.edrc.landgis.workflow.WorkflowDecision;
import cd.edrc.landgis.workflow.WorkflowTask;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisteredDeviceService {
    private static final String WORKFLOW_TYPE = "REGISTERED_DEVICE_LIFECYCLE";
    private static final String TARGET_TYPE = "registered-device";

    private final RegisteredDeviceRepository devices;
    private final AuditService auditService;
    private final WorkflowTaskService workflowTasks;

    public RegisteredDeviceService(
            RegisteredDeviceRepository devices,
            AuditService auditService,
            WorkflowTaskService workflowTasks) {
        this.devices = devices;
        this.auditService = auditService;
        this.workflowTasks = workflowTasks;
    }

    @Transactional
    public RegisteredDeviceResponse enroll(CreateRegisteredDeviceRequest request, AuthenticatedActor actor) {
        RegisteredDevice device = devices.save(new RegisteredDevice(
                UUID.randomUUID(),
                request.deviceId(),
                request.assignedUserId(),
                request.organizationId(),
                request.deviceType(),
                request.expiresAt()));
        auditDeviceLifecycle("device.enrolled", device, actor);
        return RegisteredDeviceResponse.from(device);
    }

    @Transactional(readOnly = true)
    public List<RegisteredDeviceResponse> list() {
        return devices.findAllByOrderByCreatedAtDesc().stream()
                .map(RegisteredDeviceResponse::from)
                .toList();
    }

    @Transactional
    public RegisteredDeviceLifecycleResponse suspend(String deviceId, AuthenticatedActor actor) {
        return requestLifecycleChange(deviceId, "SUSPEND", actor);
    }

    @Transactional
    public RegisteredDeviceLifecycleResponse revoke(String deviceId, AuthenticatedActor actor) {
        return requestLifecycleChange(deviceId, "REVOKE", actor);
    }

    @Transactional
    public RegisteredDeviceLifecycleResponse expire(String deviceId, AuthenticatedActor actor) {
        return requestLifecycleChange(deviceId, "EXPIRE", actor);
    }

    @Transactional
    public RegisteredDeviceLifecycleResponse decideLifecycleTask(
            UUID taskId,
            DecideWorkflowTaskRequest request,
            AuthenticatedActor actor) {
        requireSupportedLifecycleTask(workflowTasks.getTask(taskId));
        WorkflowTask task = workflowTasks.decideTask(taskId, request, actor);
        RegisteredDevice device = devices.findById(task.targetId())
                .orElseThrow(() -> new RegisteredDeviceNotFoundException(task.targetId().toString()));
        if (request.decision() == WorkflowDecision.APPROVE) {
            applyApprovedLifecycleChange(device, task.requestedAction(), task.id());
            auditDeviceLifecycle("device.lifecycle-approved", device, actor, task.requestedAction(), task.id());
        } else {
            auditDeviceLifecycle("device.lifecycle-rejected", device, actor, task.requestedAction(), task.id());
        }
        return new RegisteredDeviceLifecycleResponse(
                RegisteredDeviceResponse.from(device),
                task.requestedAction(),
                task.status().name(),
                task.id());
    }

    private RegisteredDeviceLifecycleResponse requestLifecycleChange(
            String deviceId,
            String requestedAction,
            AuthenticatedActor actor) {
        RegisteredDevice device = requireDevice(deviceId);
        UUID taskId = workflowTasks.openRegisteredDeviceLifecycleTask(device.id(), requestedAction, actor);
        auditDeviceLifecycle("device.lifecycle-requested", device, actor, requestedAction, taskId);
        return new RegisteredDeviceLifecycleResponse(
                RegisteredDeviceResponse.from(device),
                requestedAction,
                "OPEN",
                taskId);
    }

    private RegisteredDevice requireDevice(String deviceId) {
        return devices.findByDeviceId(deviceId)
                .orElseThrow(() -> new RegisteredDeviceNotFoundException(deviceId));
    }

    private void requireSupportedLifecycleTask(WorkflowTask task) {
        if (!WORKFLOW_TYPE.equals(task.workflowType()) || !TARGET_TYPE.equals(task.targetType())) {
            throw new UnsupportedWorkflowTaskException(task.id());
        }
    }

    private void applyApprovedLifecycleChange(RegisteredDevice device, String requestedAction, UUID taskId) {
        switch (requestedAction) {
            case "SUSPEND" -> device.suspend();
            case "REVOKE" -> device.revoke();
            case "EXPIRE" -> device.expire();
            default -> throw new UnsupportedWorkflowTaskException(taskId);
        }
    }

    private void auditDeviceLifecycle(String action, RegisteredDevice device, AuthenticatedActor actor) {
        auditDeviceLifecycle(action, device, actor, null, null);
    }

    private void auditDeviceLifecycle(
            String action,
            RegisteredDevice device,
            AuthenticatedActor actor,
            String requestedAction,
            UUID workflowTaskId) {
        auditService.record(
                action,
                "registered-device",
                device.deviceId(),
                AuditClassification.SECURITY,
                actor.userId(),
                device.organizationId(),
                Map.of(
                        "deviceType", device.deviceType().name(),
                        "status", device.status().name(),
                        "assignedUserScoped", device.assignedUserId() != null,
                        "organizationScoped", device.organizationId() != null,
                        "requestedAction", requestedAction == null ? "" : requestedAction,
                        "workflowTaskId", workflowTaskId == null ? "" : workflowTaskId.toString()));
    }
}
