package cd.edrc.landgis.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import cd.edrc.landgis.workflow.UnsupportedWorkflowTaskException;
import cd.edrc.landgis.workflow.WorkflowDecision;
import cd.edrc.landgis.workflow.WorkflowTask;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RegisteredDeviceServiceTest {
    @Test
    void enrollsDeviceAndAudits() {
        RegisteredDeviceRepository devices = Mockito.mock(RegisteredDeviceRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        when(devices.save(any(RegisteredDevice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RegisteredDeviceService service = new RegisteredDeviceService(devices, audit, workflowTasks);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");

        RegisteredDeviceResponse response = service.enroll(new CreateRegisteredDeviceRequest(
                "FIELD-TABLET-010",
                actor.userId(),
                null,
                RegisteredDeviceType.FIELD_MOBILE,
                null), actor);

        assertThat(response.deviceId()).isEqualTo("FIELD-TABLET-010");
        assertThat(response.status()).isEqualTo(RegisteredDeviceStatus.ACTIVE.name());
        verify(audit).record(
                eq("device.enrolled"),
                eq("registered-device"),
                eq("FIELD-TABLET-010"),
                eq(AuditClassification.SECURITY),
                eq(actor.userId()),
                isNull(),
                any());
    }

    @Test
    void requestsRevocationApprovalWithoutMutatingDevice() {
        RegisteredDeviceRepository devices = Mockito.mock(RegisteredDeviceRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        UUID taskId = UUID.randomUUID();
        RegisteredDevice device = new RegisteredDevice(
                UUID.randomUUID(),
                "FIELD-TABLET-010",
                UUID.randomUUID(),
                UUID.randomUUID(),
                RegisteredDeviceType.FIELD_MOBILE,
                null);
        when(devices.findByDeviceId("FIELD-TABLET-010")).thenReturn(Optional.of(device));
        when(workflowTasks.openRegisteredDeviceLifecycleTask(device.id(), "REVOKE", new AuthenticatedActor(
                UUID.fromString("10000000-0000-0000-0000-000000000001"),
                "officer@example.test"))).thenReturn(taskId);
        RegisteredDeviceService service = new RegisteredDeviceService(devices, audit, workflowTasks);
        AuthenticatedActor actor = new AuthenticatedActor(
                UUID.fromString("10000000-0000-0000-0000-000000000001"),
                "officer@example.test");

        RegisteredDeviceLifecycleResponse response = service.revoke("FIELD-TABLET-010", actor);

        assertThat(response.device().status()).isEqualTo(RegisteredDeviceStatus.ACTIVE.name());
        assertThat(response.requestedAction()).isEqualTo("REVOKE");
        assertThat(response.workflowStatus()).isEqualTo("OPEN");
        assertThat(response.taskId()).isEqualTo(taskId);
        assertThat(device.status()).isEqualTo(RegisteredDeviceStatus.ACTIVE);
        verify(audit).record(
                eq("device.lifecycle-requested"),
                eq("registered-device"),
                eq("FIELD-TABLET-010"),
                eq(AuditClassification.SECURITY),
                eq(actor.userId()),
                eq(device.organizationId()),
                any());
    }

    @Test
    void approvedRevocationTaskMutatesDeviceAndAudits() {
        RegisteredDeviceRepository devices = Mockito.mock(RegisteredDeviceRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        UUID taskId = UUID.randomUUID();
        UUID deviceRecordId = UUID.randomUUID();
        RegisteredDevice device = new RegisteredDevice(
                deviceRecordId,
                "FIELD-TABLET-010",
                UUID.randomUUID(),
                UUID.randomUUID(),
                RegisteredDeviceType.FIELD_MOBILE,
                null);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        WorkflowTask task = new WorkflowTask(
                taskId,
                "REGISTERED_DEVICE_LIFECYCLE",
                "registered-device",
                deviceRecordId,
                "REVOKE",
                UUID.randomUUID(),
                "maker@example.test",
                "SECURITY_OFFICER");
        task.claim(actor.userId(), actor.username());
        task.approve("Approved with security evidence", actor.userId(), actor.username());
        when(workflowTasks.getTask(taskId)).thenReturn(task);
        when(workflowTasks.decideTask(
                eq(taskId),
                any(DecideWorkflowTaskRequest.class),
                eq(actor))).thenReturn(task);
        when(devices.findById(deviceRecordId)).thenReturn(Optional.of(device));
        RegisteredDeviceService service = new RegisteredDeviceService(devices, audit, workflowTasks);

        RegisteredDeviceLifecycleResponse response = service.decideLifecycleTask(
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.APPROVE, "Approved with security evidence"),
                actor);

        assertThat(response.device().status()).isEqualTo(RegisteredDeviceStatus.REVOKED.name());
        assertThat(response.device().revokedAt()).isNotNull();
        assertThat(response.workflowStatus()).isEqualTo("APPROVED");
        verify(audit).record(
                eq("device.lifecycle-approved"),
                eq("registered-device"),
                eq("FIELD-TABLET-010"),
                eq(AuditClassification.SECURITY),
                eq(actor.userId()),
                eq(device.organizationId()),
                any());
    }

    @Test
    void rejectedLifecycleTaskDoesNotMutateDevice() {
        RegisteredDeviceRepository devices = Mockito.mock(RegisteredDeviceRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        UUID taskId = UUID.randomUUID();
        UUID deviceRecordId = UUID.randomUUID();
        RegisteredDevice device = new RegisteredDevice(
                deviceRecordId,
                "FIELD-TABLET-010",
                UUID.randomUUID(),
                UUID.randomUUID(),
                RegisteredDeviceType.FIELD_MOBILE,
                null);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        WorkflowTask task = new WorkflowTask(
                taskId,
                "REGISTERED_DEVICE_LIFECYCLE",
                "registered-device",
                deviceRecordId,
                "SUSPEND",
                UUID.randomUUID(),
                "maker@example.test",
                "SECURITY_OFFICER");
        task.claim(actor.userId(), actor.username());
        task.reject("Insufficient justification", actor.userId(), actor.username());
        when(workflowTasks.getTask(taskId)).thenReturn(task);
        when(workflowTasks.decideTask(
                eq(taskId),
                any(DecideWorkflowTaskRequest.class),
                eq(actor))).thenReturn(task);
        when(devices.findById(deviceRecordId)).thenReturn(Optional.of(device));
        RegisteredDeviceService service = new RegisteredDeviceService(devices, audit, workflowTasks);

        RegisteredDeviceLifecycleResponse response = service.decideLifecycleTask(
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.REJECT, "Insufficient justification"),
                actor);

        assertThat(response.device().status()).isEqualTo(RegisteredDeviceStatus.ACTIVE.name());
        assertThat(device.status()).isEqualTo(RegisteredDeviceStatus.ACTIVE);
        verify(audit).record(
                eq("device.lifecycle-rejected"),
                eq("registered-device"),
                eq("FIELD-TABLET-010"),
                eq(AuditClassification.SECURITY),
                eq(actor.userId()),
                eq(device.organizationId()),
                any());
    }

    @Test
    void rejectsUnsupportedWorkflowTaskBeforeDecision() {
        RegisteredDeviceRepository devices = Mockito.mock(RegisteredDeviceRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        UUID taskId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        WorkflowTask task = new WorkflowTask(
                taskId,
                "PARCEL_STATUS_TRANSITION",
                "parcel",
                UUID.randomUUID(),
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                UUID.randomUUID(),
                "maker@example.test",
                "CADASTRAL_OFFICER");
        when(workflowTasks.getTask(taskId)).thenReturn(task);
        RegisteredDeviceService service = new RegisteredDeviceService(devices, audit, workflowTasks);

        assertThatThrownBy(() -> service.decideLifecycleTask(
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.APPROVE, "Wrong workflow"),
                actor))
                .isInstanceOf(UnsupportedWorkflowTaskException.class);
    }

    @Test
    void listsDevicesNewestFirstRepositoryOrder() {
        RegisteredDeviceRepository devices = Mockito.mock(RegisteredDeviceRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        RegisteredDevice device = new RegisteredDevice(
                UUID.randomUUID(),
                "FIELD-TABLET-010",
                null,
                null,
                RegisteredDeviceType.FIELD_MOBILE,
                null);
        when(devices.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(device));
        RegisteredDeviceService service = new RegisteredDeviceService(devices, audit, workflowTasks);

        List<RegisteredDeviceResponse> response = service.list();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).deviceId()).isEqualTo("FIELD-TABLET-010");
    }

    @Test
    void rejectsUnknownDeviceLifecycleChange() {
        RegisteredDeviceRepository devices = Mockito.mock(RegisteredDeviceRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        when(devices.findByDeviceId("UNKNOWN")).thenReturn(Optional.empty());
        RegisteredDeviceService service = new RegisteredDeviceService(devices, audit, workflowTasks);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");

        assertThatThrownBy(() -> service.suspend("UNKNOWN", actor))
                .isInstanceOf(RegisteredDeviceNotFoundException.class);
    }
}
