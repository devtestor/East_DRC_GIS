package cd.edrc.landgis.pilot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

class PilotReadinessServiceTest {
    @Test
    void createsPilotReadinessRecordAndAudits() {
        Fixture fixture = new Fixture();
        when(fixture.pilots.save(any(PilotReadinessRecord.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PilotReadinessResponse response = fixture.service.create(new CreatePilotReadinessRequest(
                "North Kivu controlled pilot",
                "Fictional Goma commune",
                "Provincial pilot owner",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 12, 31)), fixture.actor);

        assertThat(response.status()).isEqualTo(PilotStatus.DRAFT.name());
        assertThat(response.geographyScope()).contains("Fictional");
        verify(fixture.audit).record(
                eq("pilot.created"),
                eq("pilot-readiness-record"),
                eq(response.id().toString()),
                eq(AuditClassification.STAFF_OPERATIONAL),
                eq(fixture.actor.userId()),
                eq(null),
                any());
    }

    @Test
    void blocksGoNoGoRequestUntilEveryRequiredSignoffIsApproved() {
        Fixture fixture = new Fixture();
        PilotReadinessRecord pilot = fixture.pilot();
        when(fixture.pilots.findById(pilot.id())).thenReturn(Optional.of(pilot));
        when(fixture.signoffs.findByPilotIdOrderByCreatedAtAsc(pilot.id())).thenReturn(List.of());

        assertThatThrownBy(() -> fixture.service.requestGoNoGo(pilot.id(), fixture.actor))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("every required signoff");
    }

    @Test
    void blocksGoNoGoRequestWhileBlockingRiskIsOpen() {
        Fixture fixture = new Fixture();
        PilotReadinessRecord pilot = fixture.pilot();
        when(fixture.pilots.findById(pilot.id())).thenReturn(Optional.of(pilot));
        when(fixture.signoffs.findByPilotIdOrderByCreatedAtAsc(pilot.id()))
                .thenReturn(approvedSignoffs(pilot.id(), fixture.actor));
        when(fixture.risks.countByPilotIdAndBlockingGoLiveTrueAndStatus(pilot.id(), PilotRiskStatus.OPEN))
                .thenReturn(1L);

        assertThatThrownBy(() -> fixture.service.requestGoNoGo(pilot.id(), fixture.actor))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("blocking risks");
    }

    @Test
    void opensGoNoGoWorkflowWhenSignoffsAndRisksAreReady() {
        Fixture fixture = new Fixture();
        PilotReadinessRecord pilot = fixture.pilot();
        UUID taskId = UUID.randomUUID();
        when(fixture.pilots.findById(pilot.id())).thenReturn(Optional.of(pilot));
        when(fixture.signoffs.findByPilotIdOrderByCreatedAtAsc(pilot.id()))
                .thenReturn(approvedSignoffs(pilot.id(), fixture.actor));
        when(fixture.risks.findByPilotIdOrderByCreatedAtAsc(pilot.id())).thenReturn(List.of());
        when(fixture.evidence.findByPilotIdOrderByAddedAtDesc(pilot.id())).thenReturn(List.of());
        when(fixture.workflow.openPilotGoNoGoTask(pilot.id(), fixture.actor)).thenReturn(taskId);

        PilotWorkflowResponse response = fixture.service.requestGoNoGo(pilot.id(), fixture.actor);

        assertThat(response.workflowStatus()).isEqualTo("OPEN");
        assertThat(response.taskId()).isEqualTo(taskId);
        assertThat(response.pilot().status()).isEqualTo(PilotStatus.UNDER_REVIEW.name());
        verify(fixture.audit).record(
                eq("pilot.go-no-go-requested"),
                eq("pilot-readiness-record"),
                eq(pilot.id().toString()),
                eq(AuditClassification.STAFF_OPERATIONAL),
                eq(fixture.actor.userId()),
                eq(null),
                any());
    }

    private static List<PilotSignoff> approvedSignoffs(UUID pilotId, AuthenticatedActor actor) {
        return java.util.Arrays.stream(PilotSignoffType.values())
                .map(type -> {
                    PilotSignoff signoff = new PilotSignoff(
                            UUID.randomUUID(),
                            pilotId,
                            type,
                            "PROVINCIAL_LAND_ADMINISTRATOR",
                            "Approved " + type.name(),
                            actor.userId(),
                            actor.username());
                    signoff.approve(actor.userId(), actor.username());
                    return signoff;
                })
                .toList();
    }

    private static class Fixture {
        private final PilotReadinessRecordRepository pilots = Mockito.mock(PilotReadinessRecordRepository.class);
        private final PilotSignoffRepository signoffs = Mockito.mock(PilotSignoffRepository.class);
        private final PilotRiskRepository risks = Mockito.mock(PilotRiskRepository.class);
        private final PilotEvidenceRepository evidence = Mockito.mock(PilotEvidenceRepository.class);
        private final WorkflowTaskService workflow = Mockito.mock(WorkflowTaskService.class);
        private final AuditService audit = Mockito.mock(AuditService.class);
        private final AuthenticatedActor actor = new AuthenticatedActor(
                UUID.fromString("10000000-0000-0000-0000-000000000001"),
                "pilot.officer@example.test");
        private final PilotReadinessService service = new PilotReadinessService(
                pilots,
                signoffs,
                risks,
                evidence,
                workflow,
                audit);

        private PilotReadinessRecord pilot() {
            return new PilotReadinessRecord(
                    UUID.randomUUID(),
                    "Pilot",
                    "Fictional geography",
                    "Owner",
                    null,
                    null,
                    actor.userId(),
                    actor.username());
        }
    }
}
