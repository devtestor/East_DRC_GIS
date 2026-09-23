package cd.edrc.landgis.workflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.common.AuthenticatedActor;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class WorkflowTaskServiceTest {
    private static final AuthenticatedActor MAKER = new AuthenticatedActor(UUID.fromString("10000000-0000-0000-0000-000000000001"), "maker@example.test");
    private static final AuthenticatedActor CHECKER = new AuthenticatedActor(UUID.fromString("10000000-0000-0000-0000-000000000002"), "checker@example.test");
    private static final AuthenticatedActor APPROVER = new AuthenticatedActor(UUID.fromString("10000000-0000-0000-0000-000000000003"), "approver@example.test");
    private static final AuthenticatedActor OTHER_CHECKER = new AuthenticatedActor(UUID.fromString("10000000-0000-0000-0000-000000000004"), "other-checker@example.test");

    @Test
    void opensParcelTransitionTask() {
        WorkflowTaskRepository tasks = Mockito.mock(WorkflowTaskRepository.class);
        WorkflowTaskEvidenceLinkRepository evidence = Mockito.mock(WorkflowTaskEvidenceLinkRepository.class);
        WorkflowRoleScopeAuthorizer roleScopes = Mockito.mock(WorkflowRoleScopeAuthorizer.class);
        when(tasks.save(any(WorkflowTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        WorkflowTaskService service = new WorkflowTaskService(tasks, evidence, roleScopes, Mockito.mock(WorkflowEvidenceValidator.class));

        UUID taskId = service.openParcelTransitionTask(
                UUID.randomUUID(),
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                MAKER);

        assertThat(taskId).isNotNull();
    }

    @Test
    void opensRegisteredDeviceLifecycleTaskForSecurityOfficerReview() {
        WorkflowTaskRepository tasks = Mockito.mock(WorkflowTaskRepository.class);
        WorkflowTaskEvidenceLinkRepository evidence = Mockito.mock(WorkflowTaskEvidenceLinkRepository.class);
        WorkflowRoleScopeAuthorizer roleScopes = Mockito.mock(WorkflowRoleScopeAuthorizer.class);
        when(tasks.save(any(WorkflowTask.class))).thenAnswer(invocation -> invocation.getArgument(0));
        WorkflowTaskService service = new WorkflowTaskService(tasks, evidence, roleScopes, Mockito.mock(WorkflowEvidenceValidator.class));

        UUID taskId = service.openRegisteredDeviceLifecycleTask(
                UUID.randomUUID(),
                "REVOKE",
                MAKER);

        assertThat(taskId).isNotNull();
    }

    @Test
    void approvesOpenTaskWithDecisionMetadata() {
        UUID taskId = UUID.randomUUID();
        UUID parcelId = UUID.randomUUID();
        WorkflowTask task = new WorkflowTask(
                taskId,
                "PARCEL_STATUS_TRANSITION",
                "parcel",
                parcelId,
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                MAKER.userId(),
                MAKER.username(),
                "CADASTRAL_OFFICER");
        WorkflowTaskRepository tasks = Mockito.mock(WorkflowTaskRepository.class);
        WorkflowTaskEvidenceLinkRepository evidence = Mockito.mock(WorkflowTaskEvidenceLinkRepository.class);
        WorkflowRoleScopeAuthorizer roleScopes = Mockito.mock(WorkflowRoleScopeAuthorizer.class);
        when(tasks.findById(taskId)).thenReturn(Optional.of(task));
        when(evidence.countByTaskId(taskId)).thenReturn(1L);
        WorkflowTaskService service = new WorkflowTaskService(tasks, evidence, roleScopes, Mockito.mock(WorkflowEvidenceValidator.class));
        service.claimTask(taskId, APPROVER);

        WorkflowTask decided = service.decideTask(
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.APPROVE, "Reviewed by cadastral officer"),
                APPROVER);

        assertThat(decided.status()).isEqualTo(WorkflowTaskStatus.APPROVED);
        assertThat(decided.decisionReason()).isEqualTo("Reviewed by cadastral officer");
        assertThat(decided.decidedBy()).isEqualTo("approver@example.test");
        assertThat(decided.decidedAt()).isNotNull();
    }

    @Test
    void rejectsDecisionWhenTaskIsAlreadyClosed() {
        UUID taskId = UUID.randomUUID();
        WorkflowTask task = new WorkflowTask(
                taskId,
                "PARCEL_STATUS_TRANSITION",
                "parcel",
                UUID.randomUUID(),
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                MAKER.userId(),
                MAKER.username(),
                "CADASTRAL_OFFICER");
        task.claim(CHECKER.userId(), CHECKER.username());
        task.reject("Insufficient evidence", CHECKER.userId(), CHECKER.username());
        WorkflowTaskRepository tasks = Mockito.mock(WorkflowTaskRepository.class);
        WorkflowTaskEvidenceLinkRepository evidence = Mockito.mock(WorkflowTaskEvidenceLinkRepository.class);
        WorkflowRoleScopeAuthorizer roleScopes = Mockito.mock(WorkflowRoleScopeAuthorizer.class);
        when(tasks.findById(taskId)).thenReturn(Optional.of(task));
        WorkflowTaskService service = new WorkflowTaskService(tasks, evidence, roleScopes, Mockito.mock(WorkflowEvidenceValidator.class));

        assertThatThrownBy(() -> service.decideTask(
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.APPROVE, "Second decision"),
                APPROVER))
                .isInstanceOf(WorkflowTaskNotOpenException.class);
    }

    @Test
    void blocksRequesterFromApprovingOwnTask() {
        UUID taskId = UUID.randomUUID();
        WorkflowTask task = new WorkflowTask(
                taskId,
                "PARCEL_STATUS_TRANSITION",
                "parcel",
                UUID.randomUUID(),
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                MAKER.userId(),
                MAKER.username(),
                "CADASTRAL_OFFICER");
        WorkflowTaskRepository tasks = Mockito.mock(WorkflowTaskRepository.class);
        WorkflowTaskEvidenceLinkRepository evidence = Mockito.mock(WorkflowTaskEvidenceLinkRepository.class);
        WorkflowRoleScopeAuthorizer roleScopes = Mockito.mock(WorkflowRoleScopeAuthorizer.class);
        when(tasks.findById(taskId)).thenReturn(Optional.of(task));
        WorkflowTaskService service = new WorkflowTaskService(tasks, evidence, roleScopes, Mockito.mock(WorkflowEvidenceValidator.class));
        service.claimTask(taskId, MAKER);

        assertThatThrownBy(() -> service.decideTask(
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.APPROVE, "Self approval should fail"),
                MAKER))
                .isInstanceOf(MakerCheckerViolationException.class);
        assertThat(task.status()).isEqualTo(WorkflowTaskStatus.CLAIMED);
    }

    @Test
    void blocksApprovalWhenNoEvidenceIsLinked() {
        UUID taskId = UUID.randomUUID();
        WorkflowTask task = new WorkflowTask(
                taskId,
                "PARCEL_STATUS_TRANSITION",
                "parcel",
                UUID.randomUUID(),
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                MAKER.userId(),
                MAKER.username(),
                "CADASTRAL_OFFICER");
        WorkflowTaskRepository tasks = Mockito.mock(WorkflowTaskRepository.class);
        WorkflowTaskEvidenceLinkRepository evidence = Mockito.mock(WorkflowTaskEvidenceLinkRepository.class);
        WorkflowRoleScopeAuthorizer roleScopes = Mockito.mock(WorkflowRoleScopeAuthorizer.class);
        when(tasks.findById(taskId)).thenReturn(Optional.of(task));
        when(evidence.countByTaskId(taskId)).thenReturn(0L);
        WorkflowTaskService service = new WorkflowTaskService(tasks, evidence, roleScopes, Mockito.mock(WorkflowEvidenceValidator.class));
        service.claimTask(taskId, CHECKER);

        assertThatThrownBy(() -> service.decideTask(
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.APPROVE, "Missing evidence"),
                CHECKER))
                .isInstanceOf(WorkflowEvidenceRequiredException.class);
        assertThat(task.status()).isEqualTo(WorkflowTaskStatus.CLAIMED);
    }

    @Test
    void addsEvidenceLinkToExistingTask() {
        UUID taskId = UUID.randomUUID();
        WorkflowTask task = new WorkflowTask(
                taskId,
                "PARCEL_STATUS_TRANSITION",
                "parcel",
                UUID.randomUUID(),
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                MAKER.userId(),
                MAKER.username(),
                "CADASTRAL_OFFICER");
        WorkflowTaskRepository tasks = Mockito.mock(WorkflowTaskRepository.class);
        WorkflowTaskEvidenceLinkRepository evidence = Mockito.mock(WorkflowTaskEvidenceLinkRepository.class);
        WorkflowRoleScopeAuthorizer roleScopes = Mockito.mock(WorkflowRoleScopeAuthorizer.class);
        when(tasks.findById(taskId)).thenReturn(Optional.of(task));
        when(evidence.save(any(WorkflowTaskEvidenceLink.class))).thenAnswer(invocation -> invocation.getArgument(0));
        WorkflowTaskService service = new WorkflowTaskService(tasks, evidence, roleScopes, Mockito.mock(WorkflowEvidenceValidator.class));

        WorkflowTaskEvidenceResponse response = service.addEvidence(
                taskId,
                new AddWorkflowTaskEvidenceRequest(
                        WorkflowEvidenceType.NOTE,
                        "manual-note",
                        null,
                        "local-note-001",
                        "Fictional review note"),
                CHECKER);

        assertThat(response.taskId()).isEqualTo(taskId);
        assertThat(response.evidenceType()).isEqualTo(WorkflowEvidenceType.NOTE.name());
        assertThat(response.addedByUserId()).isEqualTo(CHECKER.userId());
    }

    @Test
    void claimsOpenTaskForActor() {
        UUID taskId = UUID.randomUUID();
        WorkflowTask task = new WorkflowTask(
                taskId,
                "PARCEL_STATUS_TRANSITION",
                "parcel",
                UUID.randomUUID(),
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                MAKER.userId(),
                MAKER.username(),
                "CADASTRAL_OFFICER");
        WorkflowTaskRepository tasks = Mockito.mock(WorkflowTaskRepository.class);
        WorkflowTaskEvidenceLinkRepository evidence = Mockito.mock(WorkflowTaskEvidenceLinkRepository.class);
        WorkflowRoleScopeAuthorizer roleScopes = Mockito.mock(WorkflowRoleScopeAuthorizer.class);
        when(tasks.findById(taskId)).thenReturn(Optional.of(task));
        WorkflowTaskService service = new WorkflowTaskService(tasks, evidence, roleScopes, Mockito.mock(WorkflowEvidenceValidator.class));

        WorkflowTaskResponse response = service.claimTask(taskId, CHECKER);

        assertThat(response.status()).isEqualTo(WorkflowTaskStatus.CLAIMED.name());
        assertThat(response.assignedToUserId()).isEqualTo(CHECKER.userId());
        assertThat(response.assignedToActor()).isEqualTo(CHECKER.username());
        assertThat(response.claimedAt()).isNotNull();
    }

    @Test
    void blocksDecisionBeforeTaskIsClaimed() {
        UUID taskId = UUID.randomUUID();
        WorkflowTask task = new WorkflowTask(
                taskId,
                "PARCEL_STATUS_TRANSITION",
                "parcel",
                UUID.randomUUID(),
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                MAKER.userId(),
                MAKER.username(),
                "CADASTRAL_OFFICER");
        WorkflowTaskRepository tasks = Mockito.mock(WorkflowTaskRepository.class);
        WorkflowTaskEvidenceLinkRepository evidence = Mockito.mock(WorkflowTaskEvidenceLinkRepository.class);
        WorkflowRoleScopeAuthorizer roleScopes = Mockito.mock(WorkflowRoleScopeAuthorizer.class);
        when(tasks.findById(taskId)).thenReturn(Optional.of(task));
        WorkflowTaskService service = new WorkflowTaskService(tasks, evidence, roleScopes, Mockito.mock(WorkflowEvidenceValidator.class));

        assertThatThrownBy(() -> service.decideTask(
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.APPROVE, "No claim"),
                CHECKER))
                .isInstanceOf(WorkflowTaskAssignmentRequiredException.class);
    }

    @Test
    void blocksDecisionByActorWhoDidNotClaimTask() {
        UUID taskId = UUID.randomUUID();
        WorkflowTask task = new WorkflowTask(
                taskId,
                "PARCEL_STATUS_TRANSITION",
                "parcel",
                UUID.randomUUID(),
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                MAKER.userId(),
                MAKER.username(),
                "CADASTRAL_OFFICER");
        WorkflowTaskRepository tasks = Mockito.mock(WorkflowTaskRepository.class);
        WorkflowTaskEvidenceLinkRepository evidence = Mockito.mock(WorkflowTaskEvidenceLinkRepository.class);
        WorkflowRoleScopeAuthorizer roleScopes = Mockito.mock(WorkflowRoleScopeAuthorizer.class);
        when(tasks.findById(taskId)).thenReturn(Optional.of(task));
        WorkflowTaskService service = new WorkflowTaskService(tasks, evidence, roleScopes, Mockito.mock(WorkflowEvidenceValidator.class));
        service.claimTask(taskId, CHECKER);

        assertThatThrownBy(() -> service.decideTask(
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.APPROVE, "Wrong actor"),
                OTHER_CHECKER))
                .isInstanceOf(WorkflowTaskAssignedToAnotherActorException.class);
    }

    @Test
    void blocksClaimWhenActorLacksRequiredRoleScope() {
        UUID taskId = UUID.randomUUID();
        WorkflowTask task = new WorkflowTask(
                taskId,
                "PARCEL_STATUS_TRANSITION",
                "parcel",
                UUID.randomUUID(),
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                MAKER.userId(),
                MAKER.username(),
                "CADASTRAL_OFFICER");
        WorkflowTaskRepository tasks = Mockito.mock(WorkflowTaskRepository.class);
        WorkflowTaskEvidenceLinkRepository evidence = Mockito.mock(WorkflowTaskEvidenceLinkRepository.class);
        WorkflowRoleScopeAuthorizer roleScopes = Mockito.mock(WorkflowRoleScopeAuthorizer.class);
        when(tasks.findById(taskId)).thenReturn(Optional.of(task));
        doThrow(new WorkflowRoleScopeViolationException(CHECKER.userId(), "CADASTRAL_OFFICER"))
                .when(roleScopes)
                .requireClaimScope(CHECKER, task);
        WorkflowTaskService service = new WorkflowTaskService(tasks, evidence, roleScopes, Mockito.mock(WorkflowEvidenceValidator.class));

        assertThatThrownBy(() -> service.claimTask(taskId, CHECKER))
                .isInstanceOf(WorkflowRoleScopeViolationException.class);
        assertThat(task.status()).isEqualTo(WorkflowTaskStatus.OPEN);
    }
}
