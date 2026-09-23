package cd.edrc.landgis.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.workflow.AddWorkflowTaskEvidenceRequest;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import cd.edrc.landgis.workflow.WorkflowDecision;
import cd.edrc.landgis.workflow.WorkflowEvidenceRequiredException;
import cd.edrc.landgis.workflow.WorkflowEvidenceType;
import cd.edrc.landgis.workflow.WorkflowEvidenceValidator;
import cd.edrc.landgis.workflow.WorkflowRoleScopeAuthorizer;
import cd.edrc.landgis.workflow.WorkflowTask;
import cd.edrc.landgis.workflow.WorkflowTaskEvidenceLink;
import cd.edrc.landgis.workflow.WorkflowTaskEvidenceLinkRepository;
import cd.edrc.landgis.workflow.WorkflowTaskRepository;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import cd.edrc.landgis.workflow.WorkflowTaskStatus;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RegisteredDeviceLifecycleWorkflowTest {
    private static final AuthenticatedActor MAKER = new AuthenticatedActor(
            UUID.fromString("10000000-0000-0000-0000-000000000101"),
            "device-maker@example.test");
    private static final AuthenticatedActor CHECKER = new AuthenticatedActor(
            UUID.fromString("10000000-0000-0000-0000-000000000102"),
            "security-checker@example.test");

    @Test
    void deviceRevocationRequiresClaimEvidenceAndCheckerApprovalBeforeMutation() {
        InMemoryDeviceRepository deviceRepository = new InMemoryDeviceRepository();
        InMemoryWorkflowRepositories workflowRepositories = new InMemoryWorkflowRepositories();
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = workflowRepositories.workflowTaskService();
        RegisteredDeviceService devices = new RegisteredDeviceService(
                deviceRepository.repository(),
                audit,
                workflowTasks);

        RegisteredDeviceResponse enrolled = devices.enroll(new CreateRegisteredDeviceRequest(
                "FIELD-TABLET-777",
                MAKER.userId(),
                UUID.fromString("00000000-0000-0000-0000-000000000201"),
                RegisteredDeviceType.FIELD_MOBILE,
                null), MAKER);

        RegisteredDeviceLifecycleResponse requested = devices.revoke("FIELD-TABLET-777", MAKER);

        assertThat(enrolled.status()).isEqualTo(RegisteredDeviceStatus.ACTIVE.name());
        assertThat(requested.device().status()).isEqualTo(RegisteredDeviceStatus.ACTIVE.name());
        assertThat(requested.workflowStatus()).isEqualTo(WorkflowTaskStatus.OPEN.name());
        assertThat(requested.requestedAction()).isEqualTo("REVOKE");
        assertThat(deviceRepository.device("FIELD-TABLET-777").status()).isEqualTo(RegisteredDeviceStatus.ACTIVE);

        workflowTasks.claimTask(requested.taskId(), CHECKER);
        assertThatThrownBy(() -> devices.decideLifecycleTask(
                requested.taskId(),
                new DecideWorkflowTaskRequest(WorkflowDecision.APPROVE, "Evidence missing"),
                CHECKER))
                .isInstanceOf(WorkflowEvidenceRequiredException.class);
        assertThat(deviceRepository.device("FIELD-TABLET-777").status()).isEqualTo(RegisteredDeviceStatus.ACTIVE);

        workflowTasks.addEvidence(
                requested.taskId(),
                new AddWorkflowTaskEvidenceRequest(
                        WorkflowEvidenceType.NOTE,
                        "security-review-note",
                        null,
                        "SECURITY-REVIEW-777",
                        "Fictional security officer reviewed the revocation request"),
                CHECKER);
        RegisteredDeviceLifecycleResponse approved = devices.decideLifecycleTask(
                requested.taskId(),
                new DecideWorkflowTaskRequest(WorkflowDecision.APPROVE, "Approved with security evidence"),
                CHECKER);

        assertThat(approved.workflowStatus()).isEqualTo(WorkflowTaskStatus.APPROVED.name());
        assertThat(approved.device().status()).isEqualTo(RegisteredDeviceStatus.REVOKED.name());
        assertThat(approved.device().revokedAt()).isNotNull();
        assertThat(deviceRepository.device("FIELD-TABLET-777").status()).isEqualTo(RegisteredDeviceStatus.REVOKED);
        verify(audit).record(
                eq("device.lifecycle-requested"),
                eq("registered-device"),
                eq("FIELD-TABLET-777"),
                eq(AuditClassification.SECURITY),
                eq(MAKER.userId()),
                eq(UUID.fromString("00000000-0000-0000-0000-000000000201")),
                any());
        verify(audit).record(
                eq("device.lifecycle-approved"),
                eq("registered-device"),
                eq("FIELD-TABLET-777"),
                eq(AuditClassification.SECURITY),
                eq(CHECKER.userId()),
                eq(UUID.fromString("00000000-0000-0000-0000-000000000201")),
                any());
    }

    private static final class InMemoryDeviceRepository {
        private final Map<UUID, RegisteredDevice> byId = new LinkedHashMap<>();
        private final Map<String, RegisteredDevice> byDeviceId = new LinkedHashMap<>();
        private final RegisteredDeviceRepository repository = Mockito.mock(RegisteredDeviceRepository.class);

        private InMemoryDeviceRepository() {
            when(repository.save(any(RegisteredDevice.class))).thenAnswer(invocation -> {
                RegisteredDevice device = invocation.getArgument(0);
                byId.put(device.id(), device);
                byDeviceId.put(device.deviceId(), device);
                return device;
            });
            when(repository.findByDeviceId(any())).thenAnswer(invocation -> Optional.ofNullable(
                    byDeviceId.get(invocation.getArgument(0, String.class))));
            when(repository.findById(any())).thenAnswer(invocation -> Optional.ofNullable(
                    byId.get(invocation.getArgument(0, UUID.class))));
        }

        RegisteredDeviceRepository repository() {
            return repository;
        }

        RegisteredDevice device(String deviceId) {
            return byDeviceId.get(deviceId);
        }
    }

    private static final class InMemoryWorkflowRepositories {
        private final Map<UUID, WorkflowTask> tasks = new LinkedHashMap<>();
        private final List<WorkflowTaskEvidenceLink> evidenceLinks = new ArrayList<>();
        private final WorkflowTaskRepository taskRepository = Mockito.mock(WorkflowTaskRepository.class);
        private final WorkflowTaskEvidenceLinkRepository evidenceRepository =
                Mockito.mock(WorkflowTaskEvidenceLinkRepository.class);

        private InMemoryWorkflowRepositories() {
            when(taskRepository.save(any(WorkflowTask.class))).thenAnswer(invocation -> {
                WorkflowTask task = invocation.getArgument(0);
                tasks.put(task.id(), task);
                return task;
            });
            when(taskRepository.findById(any())).thenAnswer(invocation -> Optional.ofNullable(
                    tasks.get(invocation.getArgument(0, UUID.class))));
            when(taskRepository.findByStatusOrderByCreatedAtAsc(any())).thenAnswer(invocation -> tasks.values().stream()
                    .filter(task -> task.status() == invocation.getArgument(0, WorkflowTaskStatus.class))
                    .toList());

            when(evidenceRepository.save(any(WorkflowTaskEvidenceLink.class))).thenAnswer(invocation -> {
                WorkflowTaskEvidenceLink evidence = invocation.getArgument(0);
                evidenceLinks.add(evidence);
                return evidence;
            });
            when(evidenceRepository.countByTaskId(any())).thenAnswer(invocation -> evidenceLinks.stream()
                    .filter(evidence -> evidence.taskId().equals(invocation.getArgument(0, UUID.class)))
                    .count());
            when(evidenceRepository.findByTaskIdOrderByAddedAtAsc(any())).thenAnswer(invocation -> evidenceLinks.stream()
                    .filter(evidence -> evidence.taskId().equals(invocation.getArgument(0, UUID.class)))
                    .toList());
        }

        WorkflowTaskService workflowTaskService() {
            return new WorkflowTaskService(
                    taskRepository,
                    evidenceRepository,
                    Mockito.mock(WorkflowRoleScopeAuthorizer.class),
                    Mockito.mock(WorkflowEvidenceValidator.class));
        }
    }
}
