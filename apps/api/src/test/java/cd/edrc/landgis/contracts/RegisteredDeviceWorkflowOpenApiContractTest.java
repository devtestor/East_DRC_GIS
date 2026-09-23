package cd.edrc.landgis.contracts;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RegisteredDeviceWorkflowOpenApiContractTest {
    private static final Path CONTRACT = Path.of(
            "..",
            "..",
            "contracts",
            "openapi",
            "registered-device-workflow.yaml");

    @Test
    void documentsRegisteredDeviceLifecycleWorkflowEndpointsAndSchemas() throws IOException {
        String contract = Files.readString(CONTRACT);

        assertThat(contract)
                .contains("/api/v1/devices/{deviceId}/suspend:")
                .contains("/api/v1/devices/{deviceId}/revoke:")
                .contains("/api/v1/devices/{deviceId}/expire:")
                .contains("/api/v1/devices/tasks/{taskId}/decisions:")
                .contains("/api/v1/workflow/tasks/{taskId}/claim:")
                .contains("/api/v1/workflow/tasks/{taskId}/evidence:")
                .contains("RegisteredDeviceLifecycleResponse:")
                .contains("WorkflowTaskEvidenceResponse:")
                .contains("Workflow evidence required")
                .contains("REGISTERED_DEVICE_LIFECYCLE")
                .contains("SUSPEND")
                .contains("REVOKE")
                .contains("EXPIRE");
    }
}
