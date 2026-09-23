package cd.edrc.landgis.parcels;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import cd.edrc.landgis.workflow.WorkflowDecision;
import cd.edrc.landgis.workflow.WorkflowTask;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class ParcelGeometryServiceTest {
    private static final AuthenticatedActor MAKER = new AuthenticatedActor(
            UUID.fromString("30000000-0000-0000-0000-000000000001"),
            "maker@example.test");
    private static final AuthenticatedActor CHECKER = new AuthenticatedActor(
            UUID.fromString("30000000-0000-0000-0000-000000000002"),
            "checker@example.test");

    @Test
    void createsDraftGeometryVersionWithoutPublishingCurrentGeometry() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        ParcelGeometryVersionResponse stored = new ParcelGeometryVersionResponse(
                UUID.randomUUID(),
                parcelId,
                "MULTIPOLYGON(((29.1 -1.6,29.2 -1.6,29.2 -1.5,29.1 -1.5,29.1 -1.6)))",
                "DRAFT",
                new BigDecimal("123.45"),
                "survey-plan-demo",
                null,
                null,
                null,
                null,
                OffsetDateTime.now());
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.queryForObject(
                anyString(),
                ArgumentMatchers.<RowMapper<ParcelGeometryVersionResponse>>any(),
                any(),
                eq(parcelId),
                anyString(),
                eq("survey-plan-demo"))).thenReturn(stored);
        ParcelGeometryService service = new ParcelGeometryService(parcels, jdbc, audit, workflowTasks);

        ParcelGeometryVersionResponse response = service.createDraftGeometry(
                parcelId,
                new CreateParcelGeometryVersionRequest(
                        "MULTIPOLYGON(((29.1 -1.6,29.2 -1.6,29.2 -1.5,29.1 -1.5,29.1 -1.6)))",
                        "survey-plan-demo"));

        assertThat(response.geometryStatus()).isEqualTo("DRAFT");
        assertThat(response.effectiveFrom()).isNull();
        assertThat(response.approvedBy()).isNull();
        verify(audit).record(
                "parcel.geometry-draft-created",
                "parcel",
                parcelId.toString(),
                AuditClassification.STAFF_OPERATIONAL);
    }

    @Test
    void listsGeometryHistoryForExistingParcel() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        ParcelGeometryVersionResponse version = new ParcelGeometryVersionResponse(
                UUID.randomUUID(),
                parcelId,
                "MULTIPOLYGON(((29.1 -1.6,29.2 -1.6,29.2 -1.5,29.1 -1.5,29.1 -1.6)))",
                "DRAFT",
                new BigDecimal("123.45"),
                "survey-plan-demo",
                null,
                null,
                null,
                null,
                OffsetDateTime.now());
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.query(
                anyString(),
                ArgumentMatchers.<RowMapper<ParcelGeometryVersionResponse>>any(),
                eq(parcelId))).thenReturn(List.of(version));
        ParcelGeometryService service = new ParcelGeometryService(parcels, jdbc, audit, workflowTasks);

        List<ParcelGeometryVersionResponse> response = service.listGeometryVersions(parcelId);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).parcelId()).isEqualTo(parcelId);
    }

    @Test
    void rejectsGeometryForUnknownParcelBeforeWriting() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        when(parcels.existsById(parcelId)).thenReturn(false);
        ParcelGeometryService service = new ParcelGeometryService(parcels, jdbc, audit, workflowTasks);

        assertThatThrownBy(() -> service.createDraftGeometry(
                parcelId,
                new CreateParcelGeometryVersionRequest(
                        "MULTIPOLYGON(((29.1 -1.6,29.2 -1.6,29.2 -1.5,29.1 -1.5,29.1 -1.6)))",
                        "survey-plan-demo")))
                .isInstanceOf(ParcelNotFoundException.class);
        verify(jdbc, never()).queryForObject(
                anyString(),
                ArgumentMatchers.<RowMapper<ParcelGeometryVersionResponse>>any(),
                any(),
                any(),
                any(),
                any());
    }

    @Test
    void wrapsDatabaseGeometryValidationFailure() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.queryForObject(
                anyString(),
                ArgumentMatchers.<RowMapper<ParcelGeometryVersionResponse>>any(),
                any(),
                eq(parcelId),
                anyString(),
                eq("survey-plan-demo"))).thenThrow(new DataIntegrityViolationException("invalid geometry"));
        ParcelGeometryService service = new ParcelGeometryService(parcels, jdbc, audit, workflowTasks);

        assertThatThrownBy(() -> service.createDraftGeometry(
                parcelId,
                new CreateParcelGeometryVersionRequest("NOT_A_GEOMETRY", "survey-plan-demo")))
                .isInstanceOf(InvalidParcelGeometryException.class);
    }

    @Test
    void opensGeometryApprovalTaskForDraftGeometry() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        UUID parcelId = UUID.randomUUID();
        UUID geometryId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        ParcelGeometryVersionResponse geometry = geometry(geometryId, parcelId, "DRAFT");
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.query(
                anyString(),
                ArgumentMatchers.<RowMapper<ParcelGeometryVersionResponse>>any(),
                eq(geometryId))).thenReturn(List.of(geometry));
        when(workflowTasks.openParcelGeometryApprovalTask(geometryId, MAKER)).thenReturn(taskId);
        ParcelGeometryService service = new ParcelGeometryService(parcels, jdbc, audit, workflowTasks);

        ParcelGeometryApprovalResponse response = service.requestApproval(parcelId, geometryId, MAKER);

        assertThat(response.taskId()).isEqualTo(taskId);
        assertThat(response.workflowStatus()).isEqualTo("OPEN");
        assertThat(response.geometry().geometryStatus()).isEqualTo("DRAFT");
        verify(audit).record(
                "parcel.geometry-approval-requested",
                "parcel",
                parcelId.toString(),
                AuditClassification.STAFF_OPERATIONAL);
    }

    @Test
    void approvingGeometryTaskSupersedesPriorCurrentGeometryAndPublishesSelectedVersion() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        UUID parcelId = UUID.randomUUID();
        UUID geometryId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        WorkflowTask task = new WorkflowTask(
                taskId,
                "PARCEL_GEOMETRY_APPROVAL",
                "parcel-geometry-version",
                geometryId,
                "APPROVE_CURRENT_GEOMETRY",
                MAKER.userId(),
                MAKER.username(),
                "CADASTRAL_OFFICER");
        task.claim(CHECKER.userId(), CHECKER.username());
        task.approve("Cadastral review complete", CHECKER.userId(), CHECKER.username());
        ParcelGeometryVersionResponse draft = geometry(geometryId, parcelId, "DRAFT");
        ParcelGeometryVersionResponse approved = new ParcelGeometryVersionResponse(
                geometryId,
                parcelId,
                draft.geometryWkt(),
                "APPROVED",
                draft.calculatedAreaSquareMeters(),
                draft.source(),
                OffsetDateTime.now(),
                null,
                CHECKER.userId(),
                OffsetDateTime.now(),
                draft.createdAt());
        when(workflowTasks.getTask(taskId)).thenReturn(task);
        when(workflowTasks.decideTask(eq(taskId), any(DecideWorkflowTaskRequest.class), eq(CHECKER))).thenReturn(task);
        when(jdbc.query(
                anyString(),
                ArgumentMatchers.<RowMapper<ParcelGeometryVersionResponse>>any(),
                eq(geometryId))).thenReturn(List.of(draft), List.of(approved));
        when(jdbc.update(
                ArgumentMatchers.contains("geometry_status = 'SUPERSEDED'"),
                eq(parcelId))).thenReturn(1);
        when(jdbc.update(
                ArgumentMatchers.contains("geometry_status = 'APPROVED'"),
                eq(CHECKER.userId()),
                eq(geometryId),
                eq(parcelId))).thenReturn(1);
        ParcelGeometryService service = new ParcelGeometryService(parcels, jdbc, audit, workflowTasks);

        ParcelGeometryApprovalResponse response = service.decideApprovalTask(
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.APPROVE, "Cadastral review complete"),
                CHECKER);

        assertThat(response.geometry().geometryStatus()).isEqualTo("APPROVED");
        assertThat(response.workflowStatus()).isEqualTo("APPROVED");
        verify(audit).record(
                "parcel.geometry-approved-current",
                "parcel",
                parcelId.toString(),
                AuditClassification.STAFF_OPERATIONAL);
    }

    @Test
    void rejectingGeometryTaskMarksProposalRejectedWithoutPublishing() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        UUID parcelId = UUID.randomUUID();
        UUID geometryId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        WorkflowTask task = new WorkflowTask(
                taskId,
                "PARCEL_GEOMETRY_APPROVAL",
                "parcel-geometry-version",
                geometryId,
                "APPROVE_CURRENT_GEOMETRY",
                MAKER.userId(),
                MAKER.username(),
                "CADASTRAL_OFFICER");
        task.claim(CHECKER.userId(), CHECKER.username());
        task.reject("Topology issue", CHECKER.userId(), CHECKER.username());
        ParcelGeometryVersionResponse draft = geometry(geometryId, parcelId, "DRAFT");
        ParcelGeometryVersionResponse rejected = geometry(geometryId, parcelId, "REJECTED");
        when(workflowTasks.getTask(taskId)).thenReturn(task);
        when(workflowTasks.decideTask(eq(taskId), any(DecideWorkflowTaskRequest.class), eq(CHECKER))).thenReturn(task);
        when(jdbc.query(
                anyString(),
                ArgumentMatchers.<RowMapper<ParcelGeometryVersionResponse>>any(),
                eq(geometryId))).thenReturn(List.of(draft), List.of(rejected));
        when(jdbc.update(
                ArgumentMatchers.contains("geometry_status = 'REJECTED'"),
                eq(geometryId))).thenReturn(1);
        ParcelGeometryService service = new ParcelGeometryService(parcels, jdbc, audit, workflowTasks);

        ParcelGeometryApprovalResponse response = service.decideApprovalTask(
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.REJECT, "Topology issue"),
                CHECKER);

        assertThat(response.geometry().geometryStatus()).isEqualTo("REJECTED");
        assertThat(response.workflowStatus()).isEqualTo("REJECTED");
        verify(audit).record(
                "parcel.geometry-approval-rejected",
                "parcel",
                parcelId.toString(),
                AuditClassification.STAFF_OPERATIONAL);
    }

    private static ParcelGeometryVersionResponse geometry(UUID geometryId, UUID parcelId, String status) {
        return new ParcelGeometryVersionResponse(
                geometryId,
                parcelId,
                "MULTIPOLYGON(((29.1 -1.6,29.2 -1.6,29.2 -1.5,29.1 -1.5,29.1 -1.6)))",
                status,
                new BigDecimal("123.45"),
                "survey-plan-demo",
                null,
                null,
                null,
                null,
                OffsetDateTime.now());
    }
}
