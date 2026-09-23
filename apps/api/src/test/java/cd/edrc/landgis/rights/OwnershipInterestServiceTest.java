package cd.edrc.landgis.rights;

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
import cd.edrc.landgis.disputes.ActiveParcelRestrictionException;
import cd.edrc.landgis.disputes.ParcelRestrictionService;
import cd.edrc.landgis.parcels.ParcelNotFoundException;
import cd.edrc.landgis.parcels.ParcelRepository;
import cd.edrc.landgis.parties.DataConfidence;
import cd.edrc.landgis.parties.PartyResponse;
import cd.edrc.landgis.parties.PartyService;
import cd.edrc.landgis.parties.PartyType;
import cd.edrc.landgis.parties.PartyVerificationStatus;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import cd.edrc.landgis.workflow.WorkflowDecision;
import cd.edrc.landgis.workflow.WorkflowTask;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class OwnershipInterestServiceTest {
    @Test
    void createsClaimWithoutTreatingItAsRegisteredTitle() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        PartyService parties = Mockito.mock(PartyService.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        ParcelRestrictionService restrictions = Mockito.mock(ParcelRestrictionService.class);
        UUID parcelId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(parties.find(partyId)).thenReturn(Optional.of(new PartyResponse(
                partyId,
                PartyType.INDIVIDUAL.name(),
                "Fictional Claimant",
                DataConfidence.SELF_DECLARED.name(),
                PartyVerificationStatus.PENDING.name(),
                actor.userId(),
                actor.username(),
                OffsetDateTime.now())));
        OwnershipInterestResponse stored = new OwnershipInterestResponse(
                UUID.randomUUID(),
                parcelId,
                partyId,
                "Fictional Claimant",
                InterestType.CUSTOMARY_CLAIM.name(),
                InterestStatus.CLAIMED.name(),
                new BigDecimal("50.00"),
                DataConfidence.SELF_DECLARED.name(),
                "interview-note",
                null,
                null,
                actor.userId(),
                actor.username(),
                OffsetDateTime.now());
        when(jdbc.queryForObject(
                anyString(),
                ArgumentMatchers.<RowMapper<OwnershipInterestResponse>>any(),
                any(),
                eq(parcelId),
                eq(partyId),
                eq(InterestType.CUSTOMARY_CLAIM.name()),
                eq(new BigDecimal("50.00")),
                eq(DataConfidence.SELF_DECLARED.name()),
                eq("interview-note"),
                eq(actor.userId()),
                eq(actor.username()))).thenReturn(stored);
        OwnershipInterestService service = new OwnershipInterestService(
                parcels, parties, jdbc, audit, workflowTasks, restrictions);

        OwnershipInterestResponse response = service.create(
                parcelId,
                new CreateOwnershipInterestRequest(
                        partyId,
                        InterestType.CUSTOMARY_CLAIM,
                        new BigDecimal("50.00"),
                        DataConfidence.SELF_DECLARED,
                        " interview-note "),
                actor);

        assertThat(response.interestStatus()).isEqualTo(InterestStatus.CLAIMED.name());
        assertThat(response.interestType()).isEqualTo(InterestType.CUSTOMARY_CLAIM.name());
        verify(audit).record(
                eq("ownership-interest.claim-created"),
                eq("parcel"),
                eq(parcelId.toString()),
                eq(AuditClassification.LEGAL_EVIDENCE),
                eq(actor.userId()),
                eq(null),
                any());
    }

    @Test
    void rejectsClaimForUnknownParcel() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        PartyService parties = Mockito.mock(PartyService.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        ParcelRestrictionService restrictions = Mockito.mock(ParcelRestrictionService.class);
        UUID parcelId = UUID.randomUUID();
        when(parcels.existsById(parcelId)).thenReturn(false);
        OwnershipInterestService service = new OwnershipInterestService(
                parcels, parties, jdbc, audit, workflowTasks, restrictions);

        assertThatThrownBy(() -> service.create(
                parcelId,
                new CreateOwnershipInterestRequest(
                        UUID.randomUUID(),
                        InterestType.OWNERSHIP_CLAIM,
                        null,
                        DataConfidence.UNVERIFIED,
                        "source"),
                new AuthenticatedActor(UUID.randomUUID(), "officer@example.test")))
                .isInstanceOf(ParcelNotFoundException.class);
    }

    @Test
    void listsClaimsForParcel() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        PartyService parties = Mockito.mock(PartyService.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        ParcelRestrictionService restrictions = Mockito.mock(ParcelRestrictionService.class);
        UUID parcelId = UUID.randomUUID();
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.query(
                anyString(),
                ArgumentMatchers.<RowMapper<OwnershipInterestResponse>>any(),
                eq(parcelId))).thenReturn(List.of());
        OwnershipInterestService service = new OwnershipInterestService(
                parcels, parties, jdbc, audit, workflowTasks, restrictions);

        List<OwnershipInterestResponse> response = service.listByParcel(parcelId);

        assertThat(response).isEmpty();
    }

    @Test
    void reportsOwnershipConflictsWithoutChangingClaims() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        PartyService parties = Mockito.mock(PartyService.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        ParcelRestrictionService restrictions = Mockito.mock(ParcelRestrictionService.class);
        UUID parcelId = UUID.randomUUID();
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.query(
                anyString(),
                ArgumentMatchers.<RowMapper<OwnershipConflictResponse>>any(),
                eq(parcelId),
                eq(parcelId)))
                .thenReturn(List.of(new OwnershipConflictResponse(
                        parcelId,
                        "COMPETING_OWNERSHIP_CLAIMS",
                        "HIGH",
                        2,
                        "Multiple active ownership claims require human review; this is not a legal ownership determination.")));
        OwnershipInterestService service = new OwnershipInterestService(
                parcels, parties, jdbc, audit, workflowTasks, restrictions);

        List<OwnershipConflictResponse> conflicts = service.listConflicts(parcelId);

        assertThat(conflicts).singleElement()
                .satisfies(conflict -> {
                    assertThat(conflict.conflictType()).isEqualTo("COMPETING_OWNERSHIP_CLAIMS");
                    assertThat(conflict.affectedInterestCount()).isEqualTo(2);
                });
    }

    @Test
    void reviewRequestMovesClaimUnderReviewAndOpensLandTitleTask() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        PartyService parties = Mockito.mock(PartyService.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        ParcelRestrictionService restrictions = Mockito.mock(ParcelRestrictionService.class);
        UUID parcelId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(workflowTasks.openOwnershipInterestReviewTask(interestId, actor)).thenReturn(taskId);
        OwnershipInterestResponse claimed = interest(interestId, parcelId, partyId, InterestStatus.CLAIMED, actor);
        OwnershipInterestResponse underReview = interest(interestId, parcelId, partyId, InterestStatus.UNDER_REVIEW, actor);
        when(jdbc.query(
                anyString(),
                ArgumentMatchers.<RowMapper<OwnershipInterestResponse>>any(),
                eq(parcelId),
                eq(interestId))).thenReturn(List.of(claimed));
        when(jdbc.queryForObject(
                anyString(),
                ArgumentMatchers.<RowMapper<OwnershipInterestResponse>>any(),
                eq(InterestStatus.UNDER_REVIEW.name()),
                eq(interestId))).thenReturn(underReview);
        OwnershipInterestService service = new OwnershipInterestService(
                parcels, parties, jdbc, audit, workflowTasks, restrictions);

        OwnershipInterestReviewResponse response = service.requestReview(parcelId, interestId, actor);

        assertThat(response.workflowStatus()).isEqualTo("OPEN");
        assertThat(response.taskId()).isEqualTo(taskId);
        assertThat(response.interest().interestStatus()).isEqualTo(InterestStatus.UNDER_REVIEW.name());
        verify(audit).record(
                eq("ownership-interest.review-requested"),
                eq("ownership-interest"),
                eq(interestId.toString()),
                eq(AuditClassification.LEGAL_EVIDENCE),
                eq(actor.userId()),
                eq(null),
                any());
    }

    @Test
    void approvedReviewTaskMarksInterestVerifiedButNotAsOfficialTitle() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        PartyService parties = Mockito.mock(PartyService.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        ParcelRestrictionService restrictions = Mockito.mock(ParcelRestrictionService.class);
        UUID parcelId = UUID.randomUUID();
        UUID interestId = UUID.randomUUID();
        UUID partyId = UUID.randomUUID();
        UUID taskId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "checker@example.test");
        WorkflowTask task = new WorkflowTask(
                taskId,
                "OWNERSHIP_INTEREST_REVIEW",
                "ownership-interest",
                interestId,
                "VERIFY_OWNERSHIP_INTEREST",
                UUID.randomUUID(),
                "maker@example.test",
                "LAND_TITLE_OFFICER");
        task.claim(actor.userId(), actor.username());
        task.approve("Evidence checked", actor.userId(), actor.username());
        when(workflowTasks.decideTask(eq(taskId), any(DecideWorkflowTaskRequest.class), eq(actor))).thenReturn(task);
        OwnershipInterestResponse verified = interest(interestId, parcelId, partyId, InterestStatus.VERIFIED, actor);
        when(jdbc.queryForObject(
                anyString(),
                ArgumentMatchers.<RowMapper<OwnershipInterestResponse>>any(),
                eq(InterestStatus.VERIFIED.name()),
                eq(interestId))).thenReturn(verified);
        when(jdbc.query(
                anyString(),
                ArgumentMatchers.<RowMapper<UUID>>any(),
                eq(interestId))).thenReturn(List.of(parcelId));
        OwnershipInterestService service = new OwnershipInterestService(
                parcels, parties, jdbc, audit, workflowTasks, restrictions);

        OwnershipInterestReviewResponse response = service.decideReviewTask(
                taskId,
                new DecideWorkflowTaskRequest(WorkflowDecision.APPROVE, "Evidence checked"),
                actor);

        assertThat(response.workflowStatus()).isEqualTo("APPROVED");
        assertThat(response.interest().interestStatus()).isEqualTo(InterestStatus.VERIFIED.name());
        verify(audit).record(
                eq("ownership-interest.review-approved"),
                eq("ownership-interest"),
                eq(interestId.toString()),
                eq(AuditClassification.LEGAL_EVIDENCE),
                eq(actor.userId()),
                eq(null),
                any());
    }

    @Test
    void activeRestrictionBlocksNewOwnershipInterestClaim() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        PartyService parties = Mockito.mock(PartyService.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflowTasks = Mockito.mock(WorkflowTaskService.class);
        ParcelRestrictionService restrictions = Mockito.mock(ParcelRestrictionService.class);
        UUID parcelId = UUID.randomUUID();
        when(parcels.existsById(parcelId)).thenReturn(true);
        Mockito.doThrow(new ActiveParcelRestrictionException(parcelId))
                .when(restrictions).requireNoOwnershipChangeBlock(parcelId);
        OwnershipInterestService service = new OwnershipInterestService(
                parcels, parties, jdbc, audit, workflowTasks, restrictions);

        assertThatThrownBy(() -> service.create(
                parcelId,
                new CreateOwnershipInterestRequest(
                        UUID.randomUUID(),
                        InterestType.OWNERSHIP_CLAIM,
                        new BigDecimal("100.00"),
                        DataConfidence.SELF_DECLARED,
                        "source"),
                new AuthenticatedActor(UUID.randomUUID(), "officer@example.test")))
                .isInstanceOf(ActiveParcelRestrictionException.class);
    }

    private static OwnershipInterestResponse interest(
            UUID interestId,
            UUID parcelId,
            UUID partyId,
            InterestStatus status,
            AuthenticatedActor actor) {
        return new OwnershipInterestResponse(
                interestId,
                parcelId,
                partyId,
                "Fictional Claimant",
                InterestType.OWNERSHIP_CLAIM.name(),
                status.name(),
                new BigDecimal("100.00"),
                DataConfidence.DOCUMENT_SUPPORTED.name(),
                "review-note",
                null,
                null,
                actor.userId(),
                actor.username(),
                OffsetDateTime.now());
    }
}
