package cd.edrc.landgis.audit;

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
@RequestMapping("/api/v1/devices")
class RegisteredDeviceController {
    private final RegisteredDeviceService devices;
    private final AuthenticatedActorResolver actors;

    RegisteredDeviceController(RegisteredDeviceService devices, AuthenticatedActorResolver actors) {
        this.devices = devices;
        this.actors = actors;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    RegisteredDeviceResponse enroll(@Valid @RequestBody CreateRegisteredDeviceRequest request, Principal principal) {
        return devices.enroll(request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping
    List<RegisteredDeviceResponse> list() {
        return devices.list();
    }

    @PostMapping("/{deviceId}/suspend")
    RegisteredDeviceLifecycleResponse suspend(@PathVariable String deviceId, Principal principal) {
        return devices.suspend(deviceId, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{deviceId}/revoke")
    RegisteredDeviceLifecycleResponse revoke(@PathVariable String deviceId, Principal principal) {
        return devices.revoke(deviceId, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{deviceId}/expire")
    RegisteredDeviceLifecycleResponse expire(@PathVariable String deviceId, Principal principal) {
        return devices.expire(deviceId, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/tasks/{taskId}/decisions")
    RegisteredDeviceLifecycleResponse decideLifecycleTask(
            @PathVariable UUID taskId,
            @Valid @RequestBody DecideWorkflowTaskRequest request,
            Principal principal) {
        return devices.decideLifecycleTask(
                taskId,
                request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }
}
