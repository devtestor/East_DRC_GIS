package cd.edrc.landgis.parcels;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.administration.AdministrativeUnit;
import cd.edrc.landgis.administration.AdministrativeUnitRepository;
import cd.edrc.landgis.administration.AdministrativeUnitType;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import cd.edrc.landgis.workflow.WorkflowDecision;
import cd.edrc.landgis.workflow.WorkflowTask;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ParcelRegistryServiceTest {
    private static final AuthenticatedActor MAKER = new AuthenticatedActor(UUID.fromString("20000000-0000-0000-0000-000000000001"), "maker@example.test");
    private static final AuthenticatedActor CHECKER = new AuthenticatedActor(UUID.fromString("20000000-0000-0000-0000-000000000002"), "checker@example.test");

    @Test
    void rejectsDuplicateActiveUpiBeforeCreatingParcel() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        ParcelIdentifierRepository identifiers = Mockito.mock(ParcelIdentifierRepository.class);
        UpiSchemeVersionRepository schemes = Mockito.mock(UpiSchemeVersionRepository.class);
        AdministrativeUnitRepository adminUnits = Mockito.mock(AdministrativeUnitRepository.class);
        PublicParcelProjectionService projection = Mockito.mock(PublicParcelProjectionService.class);
        ParcelStateTransitionPolicy transitionPolicy = Mockito.mock(ParcelStateTransitionPolicy.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID adminUnitId = UUID.randomUUID();
        AdministrativeUnit adminUnit = new AdministrativeUnit(
                adminUnitId,
                null,
                AdministrativeUnitType.COMMUNE,
                "GOMA-COMMUNE-TEST",
                "Commune fictive test",
                LocalDate.of(2026, 1, 1));

        when(adminUnits.findById(adminUnitId)).thenReturn(Optional.of(adminUnit));
        when(identifiers.findByIdentifierTypeAndIdentifierValueAndStatus(
                IdentifierType.PROPOSED_UPI,
                "NK-001-0001",
                IdentifierStatus.ACTIVE)).thenReturn(Optional.of(Mockito.mock(ParcelIdentifier.class)));

        ParcelRegistryService service = new ParcelRegistryService(parcels, identifiers, schemes, adminUnits, projection, transitionPolicy, workflowTasks, audit);

        assertThatThrownBy(() -> service.createDraft(new CreateParcelRequest(adminUnitId, "nk-001-0001", "Residential", "Customary")))
                .isInstanceOf(DuplicateUpiException.class);
    }

    @Test
    void createsDraftParcelWithNormalizedUpi() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        ParcelIdentifierRepository identifiers = Mockito.mock(ParcelIdentifierRepository.class);
        UpiSchemeVersionRepository schemes = Mockito.mock(UpiSchemeVersionRepository.class);
        AdministrativeUnitRepository adminUnits = Mockito.mock(AdministrativeUnitRepository.class);
        PublicParcelProjectionService projection = Mockito.mock(PublicParcelProjectionService.class);
        ParcelStateTransitionPolicy transitionPolicy = Mockito.mock(ParcelStateTransitionPolicy.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID adminUnitId = UUID.randomUUID();
        UpiSchemeVersion scheme = Mockito.mock(UpiSchemeVersion.class);
        AdministrativeUnit adminUnit = new AdministrativeUnit(
                adminUnitId,
                null,
                AdministrativeUnitType.COMMUNE,
                "GOMA-COMMUNE-TEST",
                "Commune fictive test",
                LocalDate.of(2026, 1, 1));

        when(adminUnits.findById(adminUnitId)).thenReturn(Optional.of(adminUnit));
        when(identifiers.findByIdentifierTypeAndIdentifierValueAndStatus(any(), any(), any())).thenReturn(Optional.empty());
        when(schemes.findFirstBySchemeCodeAndStatusOrderByEffectiveFromDesc("PROPOSED-EDRC", "ACTIVE")).thenReturn(Optional.of(scheme));
        when(scheme.id()).thenReturn(UUID.randomUUID());
        when(parcels.save(any(Parcel.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(identifiers.save(any(ParcelIdentifier.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ParcelRegistryService service = new ParcelRegistryService(parcels, identifiers, schemes, adminUnits, projection, transitionPolicy, workflowTasks, audit);

        ParcelResponse response = service.createDraft(new CreateParcelRequest(adminUnitId, "nk-001-0001", "Residential", "Customary"));

        assertThat(response.status()).isEqualTo(ParcelStatus.DRAFT.name());
        assertThat(response.proposedUpi()).isEqualTo("NK-001-0001");
        verify(identifiers).save(any(ParcelIdentifier.class));
        verify(projection).publishDraftSummary(any(Parcel.class), any(ParcelIdentifier.class), any(AdministrativeUnit.class));
    }

    @Test
    void findsParcelByProposedUpiForStaffSearch() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        ParcelIdentifierRepository identifiers = Mockito.mock(ParcelIdentifierRepository.class);
        UpiSchemeVersionRepository schemes = Mockito.mock(UpiSchemeVersionRepository.class);
        AdministrativeUnitRepository adminUnits = Mockito.mock(AdministrativeUnitRepository.class);
        PublicParcelProjectionService projection = Mockito.mock(PublicParcelProjectionService.class);
        ParcelStateTransitionPolicy transitionPolicy = Mockito.mock(ParcelStateTransitionPolicy.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        UUID adminUnitId = UUID.randomUUID();
        Parcel parcel = new Parcel(parcelId, adminUnitId, "Residential", "Customary");
        ParcelIdentifier identifier = new ParcelIdentifier(
                UUID.randomUUID(),
                parcelId,
                "NK-001-0001",
                UUID.randomUUID());

        when(identifiers.findByIdentifierTypeAndIdentifierValueAndStatus(
                IdentifierType.PROPOSED_UPI,
                "NK-001-0001",
                IdentifierStatus.ACTIVE)).thenReturn(Optional.of(identifier));
        when(parcels.findById(parcelId)).thenReturn(Optional.of(parcel));
        when(identifiers.findByParcelIdAndIdentifierTypeAndStatus(
                parcelId,
                IdentifierType.PROPOSED_UPI,
                IdentifierStatus.ACTIVE)).thenReturn(Optional.of(identifier));
        ParcelRegistryService service = new ParcelRegistryService(parcels, identifiers, schemes, adminUnits, projection, transitionPolicy, workflowTasks, audit);

        Optional<ParcelResponse> response = service.findByProposedUpi("nk-001-0001");

        assertThat(response).isPresent();
        assertThat(response.orElseThrow().id()).isEqualTo(parcelId);
        assertThat(response.orElseThrow().proposedUpi()).isEqualTo("NK-001-0001");
    }

    @Test
    void includesActiveUpiWhenListingByAdministrativeUnit() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        ParcelIdentifierRepository identifiers = Mockito.mock(ParcelIdentifierRepository.class);
        UpiSchemeVersionRepository schemes = Mockito.mock(UpiSchemeVersionRepository.class);
        AdministrativeUnitRepository adminUnits = Mockito.mock(AdministrativeUnitRepository.class);
        PublicParcelProjectionService projection = Mockito.mock(PublicParcelProjectionService.class);
        ParcelStateTransitionPolicy transitionPolicy = Mockito.mock(ParcelStateTransitionPolicy.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        UUID adminUnitId = UUID.randomUUID();
        Parcel parcel = new Parcel(parcelId, adminUnitId, "Residential", "Customary");
        ParcelIdentifier identifier = new ParcelIdentifier(
                UUID.randomUUID(),
                parcelId,
                "NK-001-0001",
                UUID.randomUUID());

        when(parcels.findByAdministrativeUnitId(adminUnitId)).thenReturn(List.of(parcel));
        when(identifiers.findByParcelIdAndIdentifierTypeAndStatus(
                parcelId,
                IdentifierType.PROPOSED_UPI,
                IdentifierStatus.ACTIVE)).thenReturn(Optional.of(identifier));
        ParcelRegistryService service = new ParcelRegistryService(parcels, identifiers, schemes, adminUnits, projection, transitionPolicy, workflowTasks, audit);

        List<ParcelResponse> response = service.findByAdministrativeUnit(adminUnitId);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).proposedUpi()).isEqualTo("NK-001-0001");
    }

    @Test
    void transitionsParcelOnlyWhenPolicyAllowsIt() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        ParcelIdentifierRepository identifiers = Mockito.mock(ParcelIdentifierRepository.class);
        UpiSchemeVersionRepository schemes = Mockito.mock(UpiSchemeVersionRepository.class);
        AdministrativeUnitRepository adminUnits = Mockito.mock(AdministrativeUnitRepository.class);
        PublicParcelProjectionService projection = Mockito.mock(PublicParcelProjectionService.class);
        ParcelStateTransitionPolicy transitionPolicy = Mockito.mock(ParcelStateTransitionPolicy.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        Parcel parcel = new Parcel(parcelId, UUID.randomUUID(), "Residential", "Customary");

        when(parcels.findById(parcelId)).thenReturn(Optional.of(parcel));
        when(transitionPolicy.decide(ParcelStatus.DRAFT, ParcelStatus.UNDER_SURVEY))
                .thenReturn(new ParcelTransitionDecision(true, false));

        ParcelRegistryService service = new ParcelRegistryService(parcels, identifiers, schemes, adminUnits, projection, transitionPolicy, workflowTasks, audit);

        ParcelTransitionResponse response = service.transitionStatus(
                parcelId,
                new TransitionParcelStatusRequest(ParcelStatus.UNDER_SURVEY),
                MAKER);

        assertThat(response.currentStatus()).isEqualTo(ParcelStatus.UNDER_SURVEY.name());
        assertThat(response.outcome()).isEqualTo("APPLIED");
        verify(audit).record("parcel.status-transitioned", "parcel", parcelId.toString(), cd.edrc.landgis.audit.AuditClassification.STAFF_OPERATIONAL);
    }

    @Test
    void opensWorkflowTaskWhenTransitionRequiresApproval() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        ParcelIdentifierRepository identifiers = Mockito.mock(ParcelIdentifierRepository.class);
        UpiSchemeVersionRepository schemes = Mockito.mock(UpiSchemeVersionRepository.class);
        AdministrativeUnitRepository adminUnits = Mockito.mock(AdministrativeUnitRepository.class);
        PublicParcelProjectionService projection = Mockito.mock(PublicParcelProjectionService.class);
        ParcelStateTransitionPolicy transitionPolicy = Mockito.mock(ParcelStateTransitionPolicy.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Parcel parcel = new Parcel(parcelId, UUID.randomUUID(), "Residential", "Customary");
        parcel.transitionTo(ParcelStatus.UNDER_SURVEY);

        when(parcels.findById(parcelId)).thenReturn(Optional.of(parcel));
        when(transitionPolicy.decide(ParcelStatus.UNDER_SURVEY, ParcelStatus.UNDER_REVIEW))
                .thenReturn(new ParcelTransitionDecision(true, true));
        when(workflowTasks.openParcelTransitionTask(
                parcelId,
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                MAKER)).thenReturn(taskId);

        ParcelRegistryService service = new ParcelRegistryService(parcels, identifiers, schemes, adminUnits, projection, transitionPolicy, workflowTasks, audit);

        ParcelTransitionResponse response = service.transitionStatus(
                parcelId,
                new TransitionParcelStatusRequest(ParcelStatus.UNDER_REVIEW),
                MAKER);

        assertThat(response.currentStatus()).isEqualTo(ParcelStatus.UNDER_SURVEY.name());
        assertThat(response.requestedStatus()).isEqualTo(ParcelStatus.UNDER_REVIEW.name());
        assertThat(response.outcome()).isEqualTo("PENDING_APPROVAL");
        assertThat(response.workflowTaskId()).isEqualTo(taskId);
        verify(audit).record("parcel.status-transition-requested", "parcel", parcelId.toString(), cd.edrc.landgis.audit.AuditClassification.STAFF_OPERATIONAL);
    }

    @Test
    void rejectsTransitionNotAllowedByPolicy() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        ParcelIdentifierRepository identifiers = Mockito.mock(ParcelIdentifierRepository.class);
        UpiSchemeVersionRepository schemes = Mockito.mock(UpiSchemeVersionRepository.class);
        AdministrativeUnitRepository adminUnits = Mockito.mock(AdministrativeUnitRepository.class);
        PublicParcelProjectionService projection = Mockito.mock(PublicParcelProjectionService.class);
        ParcelStateTransitionPolicy transitionPolicy = Mockito.mock(ParcelStateTransitionPolicy.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        Parcel parcel = new Parcel(parcelId, UUID.randomUUID(), "Residential", "Customary");

        when(parcels.findById(parcelId)).thenReturn(Optional.of(parcel));
        when(transitionPolicy.decide(ParcelStatus.DRAFT, ParcelStatus.ACTIVE)).thenReturn(ParcelTransitionDecision.denied());

        ParcelRegistryService service = new ParcelRegistryService(parcels, identifiers, schemes, adminUnits, projection, transitionPolicy, workflowTasks, audit);

        assertThatThrownBy(() -> service.transitionStatus(
                parcelId,
                new TransitionParcelStatusRequest(ParcelStatus.ACTIVE),
                MAKER))
                .isInstanceOf(InvalidParcelStateTransitionException.class);
    }

    @Test
    void approvingWorkflowTaskAppliesPendingParcelTransition() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        ParcelIdentifierRepository identifiers = Mockito.mock(ParcelIdentifierRepository.class);
        UpiSchemeVersionRepository schemes = Mockito.mock(UpiSchemeVersionRepository.class);
        AdministrativeUnitRepository adminUnits = Mockito.mock(AdministrativeUnitRepository.class);
        PublicParcelProjectionService projection = Mockito.mock(PublicParcelProjectionService.class);
        ParcelStateTransitionPolicy transitionPolicy = Mockito.mock(ParcelStateTransitionPolicy.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Parcel parcel = new Parcel(parcelId, UUID.randomUUID(), "Residential", "Customary");
        parcel.transitionTo(ParcelStatus.UNDER_SURVEY);
        WorkflowTask task = new WorkflowTask(
                taskId,
                "PARCEL_STATUS_TRANSITION",
                "parcel",
                parcelId,
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                MAKER.userId(),
                MAKER.username(),
                "CADASTRAL_OFFICER");
        task.claim(CHECKER.userId(), CHECKER.username());
        task.approve("Reviewed and accepted", CHECKER.userId(), CHECKER.username());

        when(workflowTasks.decideTask(
                any(UUID.class),
                any(DecideWorkflowTaskRequest.class),
                any(AuthenticatedActor.class))).thenReturn(task);
        when(parcels.findById(parcelId)).thenReturn(Optional.of(parcel));

        ParcelRegistryService service = new ParcelRegistryService(parcels, identifiers, schemes, adminUnits, projection, transitionPolicy, workflowTasks, audit);

        ParcelTransitionResponse response = service.decideStatusTransitionTask(
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.APPROVE, "Reviewed and accepted"),
                CHECKER);

        assertThat(response.currentStatus()).isEqualTo(ParcelStatus.UNDER_REVIEW.name());
        assertThat(response.outcome()).isEqualTo("APPROVED_AND_APPLIED");
        verify(audit).record("parcel.status-transition-approved", "parcel", parcelId.toString(), cd.edrc.landgis.audit.AuditClassification.STAFF_OPERATIONAL);
    }

    @Test
    void rejectingWorkflowTaskLeavesParcelStatusUnchanged() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        ParcelIdentifierRepository identifiers = Mockito.mock(ParcelIdentifierRepository.class);
        UpiSchemeVersionRepository schemes = Mockito.mock(UpiSchemeVersionRepository.class);
        AdministrativeUnitRepository adminUnits = Mockito.mock(AdministrativeUnitRepository.class);
        PublicParcelProjectionService projection = Mockito.mock(PublicParcelProjectionService.class);
        ParcelStateTransitionPolicy transitionPolicy = Mockito.mock(ParcelStateTransitionPolicy.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        AuditService audit = Mockito.mock(AuditService.class);
        UUID parcelId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        Parcel parcel = new Parcel(parcelId, UUID.randomUUID(), "Residential", "Customary");
        parcel.transitionTo(ParcelStatus.UNDER_SURVEY);
        WorkflowTask task = new WorkflowTask(
                taskId,
                "PARCEL_STATUS_TRANSITION",
                "parcel",
                parcelId,
                "UNDER_SURVEY_TO_UNDER_REVIEW",
                MAKER.userId(),
                MAKER.username(),
                "CADASTRAL_OFFICER");
        task.claim(CHECKER.userId(), CHECKER.username());
        task.reject("Boundary evidence incomplete", CHECKER.userId(), CHECKER.username());

        when(workflowTasks.decideTask(
                any(UUID.class),
                any(DecideWorkflowTaskRequest.class),
                any(AuthenticatedActor.class))).thenReturn(task);
        when(parcels.findById(parcelId)).thenReturn(Optional.of(parcel));

        ParcelRegistryService service = new ParcelRegistryService(parcels, identifiers, schemes, adminUnits, projection, transitionPolicy, workflowTasks, audit);

        ParcelTransitionResponse response = service.decideStatusTransitionTask(
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.REJECT, "Boundary evidence incomplete"),
                CHECKER);

        assertThat(parcel.status()).isEqualTo(ParcelStatus.UNDER_SURVEY);
        assertThat(response.outcome()).isEqualTo("REJECTED");
        verify(audit).record("parcel.status-transition-rejected", "parcel", parcelId.toString(), cd.edrc.landgis.audit.AuditClassification.STAFF_OPERATIONAL);
    }
}
