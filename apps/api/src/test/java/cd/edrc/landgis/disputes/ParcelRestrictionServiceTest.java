package cd.edrc.landgis.disputes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.parcels.ParcelNotFoundException;
import cd.edrc.landgis.parcels.ParcelRepository;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import cd.edrc.landgis.workflow.WorkflowDecision;
import cd.edrc.landgis.workflow.WorkflowTask;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class ParcelRestrictionServiceTest {
    @Test
    void createsActiveRestrictionWithAuditEvidence() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        ParcelRestrictionResponse stored = restriction(parcelId, actor);
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.queryForObject(
                anyString(),
                ArgumentMatchers.<RowMapper<ParcelRestrictionResponse>>any(),
                eq(parcelId),
                eq(RestrictionType.COURT_CAUTION.name()),
                eq("court-order"),
                eq("COURT-2026-001"),
                eq("Court caution pending hearing"),
                eq(true),
                eq(null),
                eq(actor.userId()),
                eq(actor.username()))).thenReturn(stored);
        ParcelRestrictionService service = new ParcelRestrictionService(parcels, jdbc, audit);

        ParcelRestrictionResponse response = service.create(
                parcelId,
                new CreateParcelRestrictionRequest(
                        RestrictionType.COURT_CAUTION,
                        " court-order ",
                        " COURT-2026-001 ",
                        " Court caution pending hearing ",
                        true,
                        null),
                actor);

        assertThat(response.status()).isEqualTo(RestrictionStatus.ACTIVE.name());
        assertThat(response.blocksOwnershipChanges()).isTrue();
        verify(audit).record(
                eq("parcel-restriction.applied"),
                eq("parcel"),
                eq(parcelId.toString()),
                eq(AuditClassification.LEGAL_EVIDENCE),
                eq(actor.userId()),
                eq(null),
                any());
    }

    @Test
    void rejectsRestrictionForUnknownParcel() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        when(parcels.existsById(parcelId)).thenReturn(false);
        ParcelRestrictionService service = new ParcelRestrictionService(parcels, jdbc, audit);

        assertThatThrownBy(() -> service.create(
                parcelId,
                new CreateParcelRestrictionRequest(
                        RestrictionType.OWNERSHIP_DISPUTE,
                        "complaint",
                        null,
                        "Dispute",
                        true,
                        null),
                new AuthenticatedActor(UUID.randomUUID(), "officer@example.test")))
                .isInstanceOf(ParcelNotFoundException.class);
    }

    @Test
    void activeOwnershipBlockRaisesFriendlyException() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq(parcelId))).thenReturn(1);
        ParcelRestrictionService service = new ParcelRestrictionService(parcels, jdbc, audit);

        assertThatThrownBy(() -> service.requireNoOwnershipChangeBlock(parcelId))
                .isInstanceOf(ActiveParcelRestrictionException.class);
    }

    @Test
    void listsParcelRestrictions() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.query(
                anyString(),
                ArgumentMatchers.<RowMapper<ParcelRestrictionResponse>>any(),
                eq(parcelId))).thenReturn(List.of());
        ParcelRestrictionService service = new ParcelRestrictionService(parcels, jdbc, audit);

        assertThat(service.listByParcel(parcelId)).isEmpty();
    }

    @Test
    void releaseRequestCreatesEvidenceWorkflowWithoutChangingRestriction() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflows = Mockito.mock(WorkflowTaskService.class);
        UUID parcelId = UUID.randomUUID();
        UUID restrictionId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "requester@example.test");
        ParcelRestrictionResponse stored = restrictionWithId(parcelId, restrictionId, actor, RestrictionStatus.ACTIVE.name());
        UUID taskId = UUID.randomUUID();
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.query(anyString(), ArgumentMatchers.<RowMapper<ParcelRestrictionResponse>>any(), eq(parcelId), eq(restrictionId)))
                .thenReturn(List.of(stored));
        when(workflows.openParcelRestrictionReleaseTask(restrictionId, actor)).thenReturn(taskId);
        ParcelRestrictionService service = new ParcelRestrictionService(parcels, jdbc, audit, workflows);

        ParcelRestrictionReleaseResponse response = service.requestRelease(parcelId, restrictionId, actor);

        assertThat(response.taskId()).isEqualTo(taskId);
        assertThat(response.workflowStatus()).isEqualTo("OPEN");
        verify(workflows).openParcelRestrictionReleaseTask(restrictionId, actor);
    }

    @Test
    void approvedReleaseMarksRestrictionReleased() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflows = Mockito.mock(WorkflowTaskService.class);
        UUID parcelId = UUID.randomUUID();
        UUID restrictionId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        AuthenticatedActor requester = new AuthenticatedActor(UUID.randomUUID(), "requester@example.test");
        AuthenticatedActor checker = new AuthenticatedActor(UUID.randomUUID(), "checker@example.test");
        ParcelRestrictionResponse active = restrictionWithId(parcelId, restrictionId, requester, RestrictionStatus.ACTIVE.name());
        ParcelRestrictionResponse released = restrictionWithId(parcelId, restrictionId, checker, RestrictionStatus.RELEASED.name());
        WorkflowTask existingTask = new WorkflowTask(
                taskId, "PARCEL_RESTRICTION_RELEASE", "parcel-restriction", restrictionId,
                "RELEASE_RESTRICTION", requester.userId(), requester.username(), "LAND_TITLE_OFFICER");
        WorkflowTask decidedTask = new WorkflowTask(
                taskId, "PARCEL_RESTRICTION_RELEASE", "parcel-restriction", restrictionId,
                "RELEASE_RESTRICTION", requester.userId(), requester.username(), "LAND_TITLE_OFFICER");
        decidedTask.claim(checker.userId(), checker.username());
        decidedTask.approve("Court release evidence verified", checker.userId(), checker.username());
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.query(anyString(), ArgumentMatchers.<RowMapper<ParcelRestrictionResponse>>any(), eq(parcelId), eq(restrictionId)))
                .thenReturn(List.of(active));
        when(jdbc.queryForObject(anyString(), ArgumentMatchers.<RowMapper<ParcelRestrictionResponse>>any(),
                eq(checker.userId()), eq(checker.username()), eq(restrictionId))).thenReturn(released);
        when(workflows.getTask(taskId)).thenReturn(existingTask);
        when(workflows.decideTask(eq(taskId), any(DecideWorkflowTaskRequest.class), eq(checker))).thenReturn(decidedTask);
        ParcelRestrictionService service = new ParcelRestrictionService(parcels, jdbc, audit, workflows);

        ParcelRestrictionReleaseResponse response = service.decideReleaseTask(
                parcelId,
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.APPROVE, "Court release evidence verified"),
                checker);

        assertThat(response.restriction().status()).isEqualTo(RestrictionStatus.RELEASED.name());
        assertThat(response.workflowStatus()).isEqualTo("APPROVED");
        verify(audit).record(eq("parcel-restriction.release-approved"), eq("parcel-restriction"),
                eq(restrictionId.toString()), eq(AuditClassification.LEGAL_EVIDENCE), eq(checker.userId()), eq(null), any());
    }

    @Test
    void rejectedReleaseLeavesRestrictionActive() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflows = Mockito.mock(WorkflowTaskService.class);
        UUID parcelId = UUID.randomUUID();
        UUID restrictionId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        AuthenticatedActor requester = new AuthenticatedActor(UUID.randomUUID(), "requester@example.test");
        AuthenticatedActor checker = new AuthenticatedActor(UUID.randomUUID(), "checker@example.test");
        ParcelRestrictionResponse active = restrictionWithId(parcelId, restrictionId, requester, RestrictionStatus.ACTIVE.name());
        WorkflowTask existingTask = new WorkflowTask(
                taskId, "PARCEL_RESTRICTION_RELEASE", "parcel-restriction", restrictionId,
                "RELEASE_RESTRICTION", requester.userId(), requester.username(), "LAND_TITLE_OFFICER");
        WorkflowTask decidedTask = new WorkflowTask(
                taskId, "PARCEL_RESTRICTION_RELEASE", "parcel-restriction", restrictionId,
                "RELEASE_RESTRICTION", requester.userId(), requester.username(), "LAND_TITLE_OFFICER");
        decidedTask.claim(checker.userId(), checker.username());
        decidedTask.reject("Authority evidence incomplete", checker.userId(), checker.username());
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.query(anyString(), ArgumentMatchers.<RowMapper<ParcelRestrictionResponse>>any(), eq(parcelId), eq(restrictionId)))
                .thenReturn(List.of(active));
        when(workflows.getTask(taskId)).thenReturn(existingTask);
        when(workflows.decideTask(eq(taskId), any(DecideWorkflowTaskRequest.class), eq(checker))).thenReturn(decidedTask);
        ParcelRestrictionService service = new ParcelRestrictionService(parcels, jdbc, audit, workflows);

        ParcelRestrictionReleaseResponse response = service.decideReleaseTask(
                parcelId,
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.REJECT, "Authority evidence incomplete"),
                checker);

        assertThat(response.restriction().status()).isEqualTo(RestrictionStatus.ACTIVE.name());
        verify(jdbc, Mockito.never()).queryForObject(anyString(), ArgumentMatchers.<RowMapper<ParcelRestrictionResponse>>any(),
                eq(checker.userId()), eq(checker.username()), eq(restrictionId));
    }

    private static ParcelRestrictionResponse restriction(UUID parcelId, AuthenticatedActor actor) {
        return restrictionWithId(parcelId, UUID.randomUUID(), actor, RestrictionStatus.ACTIVE.name());
    }

    private static ParcelRestrictionResponse restrictionWithId(
            UUID parcelId, UUID restrictionId, AuthenticatedActor actor, String status) {
        return new ParcelRestrictionResponse(
                restrictionId,
                parcelId,
                RestrictionType.COURT_CAUTION.name(),
                status,
                "court-order",
                "COURT-2026-001",
                "Court caution pending hearing",
                true,
                OffsetDateTime.now(),
                null,
                actor.userId(),
                actor.username(),
                OffsetDateTime.now());
    }
}
