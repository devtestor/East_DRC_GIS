package cd.edrc.landgis.disputes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.documents.DocumentResponse;
import cd.edrc.landgis.documents.DocumentService;
import cd.edrc.landgis.parcels.ParcelRepository;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import cd.edrc.landgis.workflow.WorkflowTask;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class DisputeCaseServiceTest {
    @Test
    void opensOperationalDisputeCaseWithoutAdjudication() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflows = Mockito.mock(WorkflowTaskService.class);
        UUID parcelId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        DisputeCaseResponse stored = disputeCase(parcelId, actor, DisputeCaseStatus.OPEN.name());
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.queryForObject(
                anyString(),
                ArgumentMatchers.<RowMapper<DisputeCaseResponse>>any(),
                eq(parcelId),
                eq(DisputeCaseType.OWNERSHIP_DISPUTE.name()),
                eq("Competing fictional claim"),
                eq("citizen-complaint"),
                eq("CASE-001"),
                eq(actor.userId()),
                eq(actor.username()))).thenReturn(stored);
        DisputeCaseService service = new DisputeCaseService(parcels, jdbc, audit, workflows);

        DisputeCaseResponse response = service.create(
                parcelId,
                new CreateDisputeCaseRequest(
                        DisputeCaseType.OWNERSHIP_DISPUTE,
                        " Competing fictional claim ",
                        " citizen-complaint ",
                        " CASE-001 "),
                actor);

        assertThat(response.status()).isEqualTo(DisputeCaseStatus.OPEN.name());
        verify(audit).record(
                eq("dispute-case.opened"),
                eq("dispute-case"),
                eq(response.id().toString()),
                eq(AuditClassification.LEGAL_EVIDENCE),
                eq(actor.userId()),
                eq(null),
                any());
    }

    @Test
    void linksOnlyAnAccessibleDocumentAndAuditsTheRelationship() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflows = Mockito.mock(WorkflowTaskService.class);
        DocumentService documents = Mockito.mock(DocumentService.class);
        UUID parcelId = UUID.randomUUID();
        UUID caseId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        DisputeCaseResponse disputeCase = new DisputeCaseResponse(
                caseId, parcelId, DisputeCaseType.OWNERSHIP_DISPUTE.name(), DisputeCaseStatus.OPEN.name(),
                "Summary", "complaint", null, actor.userId(), actor.username(), OffsetDateTime.now(), OffsetDateTime.now());
        DisputeDocumentResponse link = new DisputeDocumentResponse(
                UUID.randomUUID(), caseId, documentId, DisputeDocumentRelationship.COURT_ORDER.name(),
                "Supporting order", actor.userId(), actor.username(), OffsetDateTime.now());
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.query(anyString(), ArgumentMatchers.<RowMapper<DisputeCaseResponse>>any(), eq(parcelId), eq(caseId)))
                .thenReturn(List.of(disputeCase));
        when(documents.get(eq(documentId), eq(actor))).thenReturn(new DocumentResponse(
                documentId, "COURT_ORDER", "dispute-case", caseId, "Supporting order", "LEGAL_EVIDENCE",
                "PERMANENT", "workflow-task-and-authorized-staff", null, null, true,
                actor.userId(), actor.username(), OffsetDateTime.now(), null, List.of()));
        when(jdbc.queryForObject(anyString(), ArgumentMatchers.<RowMapper<DisputeDocumentResponse>>any(),
                eq(caseId), eq(documentId), eq(DisputeDocumentRelationship.COURT_ORDER.name()),
                eq("Supporting order"), eq(actor.userId()), eq(actor.username()))).thenReturn(link);
        DisputeCaseService service = new DisputeCaseService(parcels, jdbc, audit, workflows, documents);

        DisputeDocumentResponse response = service.linkDocument(
                parcelId,
                caseId,
                new LinkDisputeDocumentRequest(documentId, DisputeDocumentRelationship.COURT_ORDER, " Supporting order "),
                actor);

        assertThat(response.documentId()).isEqualTo(documentId);
        verify(documents).get(documentId, actor);
        verify(audit).record(eq("dispute-case.document-linked"), eq("dispute-case"), eq(caseId.toString()),
                eq(AuditClassification.LEGAL_EVIDENCE), eq(actor.userId()), eq(null), any());
    }

    @Test
    void schedulesHearingAndMovesCaseToHearingState() {
        ParcelRepository parcels = Mockito.mock(ParcelRepository.class);
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        WorkflowTaskService workflows = Mockito.mock(WorkflowTaskService.class);
        DocumentService documents = Mockito.mock(DocumentService.class);
        UUID parcelId = UUID.randomUUID();
        UUID caseId = UUID.randomUUID();
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        OffsetDateTime hearingTime = OffsetDateTime.now().plusDays(3);
        DisputeCaseResponse underReview = new DisputeCaseResponse(
                caseId, parcelId, DisputeCaseType.OWNERSHIP_DISPUTE.name(), DisputeCaseStatus.UNDER_REVIEW.name(),
                "Summary", "complaint", null, actor.userId(), actor.username(), OffsetDateTime.now(), OffsetDateTime.now());
        DisputeHearingResponse hearing = new DisputeHearingResponse(
                UUID.randomUUID(), caseId, hearingTime, "Commune hall", "First hearing",
                "SCHEDULED", actor.userId(), actor.username(), OffsetDateTime.now(), null);
        DisputeCaseResponse hearingCase = new DisputeCaseResponse(
                caseId, parcelId, DisputeCaseType.OWNERSHIP_DISPUTE.name(), DisputeCaseStatus.HEARING.name(),
                "Summary", "complaint", null, actor.userId(), actor.username(), OffsetDateTime.now(), OffsetDateTime.now());
        when(parcels.existsById(parcelId)).thenReturn(true);
        when(jdbc.query(anyString(), ArgumentMatchers.<RowMapper<DisputeCaseResponse>>any(), eq(parcelId), eq(caseId)))
                .thenReturn(List.of(underReview));
        when(jdbc.queryForObject(anyString(), ArgumentMatchers.<RowMapper<DisputeHearingResponse>>any(),
                eq(caseId), eq(hearingTime), eq("Commune hall"), eq("First hearing"), eq(actor.userId()), eq(actor.username())))
                .thenReturn(hearing);
        when(jdbc.queryForObject(anyString(), ArgumentMatchers.<RowMapper<DisputeCaseResponse>>any(),
                eq(DisputeCaseStatus.HEARING.name()), eq(caseId))).thenReturn(hearingCase);
        DisputeCaseService service = new DisputeCaseService(parcels, jdbc, audit, workflows, documents);

        DisputeHearingResponse response = service.scheduleHearing(
                parcelId, caseId, new ScheduleDisputeHearingRequest(hearingTime, " Commune hall ", " First hearing "), actor);

        assertThat(response.status()).isEqualTo("SCHEDULED");
        verify(audit).record(eq("dispute-case.hearing-scheduled"), eq("dispute-case"), eq(caseId.toString()),
                eq(AuditClassification.LEGAL_EVIDENCE), eq(actor.userId()), eq(null), any());
    }

    private static DisputeCaseResponse disputeCase(UUID parcelId, AuthenticatedActor actor, String status) {
        return new DisputeCaseResponse(
                UUID.randomUUID(),
                parcelId,
                DisputeCaseType.OWNERSHIP_DISPUTE.name(),
                status,
                "Competing fictional claim",
                "citizen-complaint",
                "CASE-001",
                actor.userId(),
                actor.username(),
                OffsetDateTime.now(),
                OffsetDateTime.now());
    }
}
