package cd.edrc.landgis.pilot;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import cd.edrc.landgis.workflow.UnsupportedWorkflowTaskException;
import cd.edrc.landgis.workflow.WorkflowDecision;
import cd.edrc.landgis.workflow.WorkflowTask;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PilotReadinessService {
    private static final String GO_NO_GO_WORKFLOW = "PILOT_GO_NO_GO";
    private static final String GO_NO_GO_TARGET = "pilot-readiness-record";
    private static final String SIGNOFF_WORKFLOW = "PILOT_SIGNOFF_REVIEW";
    private static final String SIGNOFF_TARGET = "pilot-signoff";

    private final PilotReadinessRecordRepository pilots;
    private final PilotSignoffRepository signoffs;
    private final PilotRiskRepository risks;
    private final PilotEvidenceRepository evidence;
    private final PilotOperationalGateRepository operationalGates;
    private final WorkflowTaskService workflowTasks;
    private final AuditService auditService;

    PilotReadinessService(
            PilotReadinessRecordRepository pilots,
            PilotSignoffRepository signoffs,
            PilotRiskRepository risks,
            PilotEvidenceRepository evidence,
            PilotOperationalGateRepository operationalGates,
            WorkflowTaskService workflowTasks,
            AuditService auditService) {
        this.pilots = pilots;
        this.signoffs = signoffs;
        this.risks = risks;
        this.evidence = evidence;
        this.operationalGates = operationalGates;
        this.workflowTasks = workflowTasks;
        this.auditService = auditService;
    }

    @Transactional
    public PilotReadinessResponse create(CreatePilotReadinessRequest request, AuthenticatedActor actor) {
        PilotReadinessRecord record = pilots.save(new PilotReadinessRecord(
                UUID.randomUUID(),
                request.title(),
                request.geographyScope(),
                request.pilotOwner(),
                request.plannedStartDate(),
                request.plannedEndDate(),
                actor.userId(),
                actor.username()));
        audit("pilot.created", record, actor, Map.of("geographyScope", record.geographyScope()));
        return summary(record);
    }

    @Transactional(readOnly = true)
    public List<PilotReadinessResponse> list() {
        return pilots.findAllByOrderByCreatedAtDesc().stream().map(this::summary).toList();
    }

    @Transactional(readOnly = true)
    public PilotReadinessResponse get(UUID pilotId) {
        return summary(requirePilot(pilotId));
    }

    @Transactional
    public PilotSignoffResponse addSignoff(UUID pilotId, CreatePilotSignoffRequest request, AuthenticatedActor actor) {
        PilotReadinessRecord pilot = requirePilot(pilotId);
        PilotSignoff signoff = signoffs.save(new PilotSignoff(
                UUID.randomUUID(),
                pilot.id(),
                request.signoffType(),
                request.requiredRole(),
                request.summary(),
                actor.userId(),
                actor.username()));
        UUID taskId = workflowTasks.openPilotSignoffTask(
                signoff.id(),
                "APPROVE_" + signoff.signoffType().name(),
                signoff.requiredRole(),
                actor);
        signoff.attachTask(taskId);
        audit("pilot.signoff-requested", pilot, actor, Map.of(
                "signoffType", signoff.signoffType().name(),
                "requiredRole", signoff.requiredRole(),
                "workflowTaskId", taskId.toString()));
        return PilotSignoffResponse.from(signoff);
    }

    @Transactional
    public PilotSignoffResponse decideSignoffTask(
            UUID taskId,
            DecideWorkflowTaskRequest request,
            AuthenticatedActor actor) {
        WorkflowTask existing = workflowTasks.getTask(taskId);
        requireTask(existing, SIGNOFF_WORKFLOW, SIGNOFF_TARGET);
        WorkflowTask task = workflowTasks.decideTask(taskId, request, actor);
        PilotSignoff signoff = signoffs.findByWorkflowTaskId(taskId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pilot signoff task not found"));
        if (request.decision() == WorkflowDecision.APPROVE) {
            signoff.approve(actor.userId(), actor.username());
        } else {
            signoff.reject(actor.userId(), actor.username());
        }
        PilotReadinessRecord pilot = requirePilot(signoff.pilotId());
        audit("pilot.signoff-decided", pilot, actor, Map.of(
                "signoffType", signoff.signoffType().name(),
                "decision", request.decision().name(),
                "workflowStatus", task.status().name()));
        return PilotSignoffResponse.from(signoff);
    }

    @Transactional
    public PilotRiskResponse addRisk(UUID pilotId, CreatePilotRiskRequest request, AuthenticatedActor actor) {
        PilotReadinessRecord pilot = requirePilot(pilotId);
        PilotRisk risk = risks.save(new PilotRisk(
                UUID.randomUUID(),
                pilot.id(),
                request.severity(),
                request.title(),
                request.mitigationPlan(),
                request.blockingGoLive(),
                actor.userId(),
                actor.username()));
        audit("pilot.risk-created", pilot, actor, Map.of(
                "severity", risk.severity().name(),
                "blockingGoLive", risk.blockingGoLive()));
        return PilotRiskResponse.from(risk);
    }

    @Transactional
    public PilotRiskResponse updateRiskStatus(
            UUID pilotId,
            UUID riskId,
            UpdatePilotRiskStatusRequest request,
            AuthenticatedActor actor) {
        PilotReadinessRecord pilot = requirePilot(pilotId);
        PilotRisk risk = risks.findById(riskId)
                .filter(candidate -> candidate.pilotId().equals(pilotId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pilot risk not found"));
        risk.updateStatus(request.status(), request.blockingGoLive());
        audit("pilot.risk-status-updated", pilot, actor, Map.of(
                "riskId", risk.id().toString(),
                "status", risk.status().name(),
                "blockingGoLive", risk.blockingGoLive()));
        return PilotRiskResponse.from(risk);
    }

    @Transactional
    public PilotEvidenceResponse addEvidence(UUID pilotId, CreatePilotEvidenceRequest request, AuthenticatedActor actor) {
        PilotReadinessRecord pilot = requirePilot(pilotId);
        if (request.referenceId() == null
                && (request.externalReference() == null || request.externalReference().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Evidence requires referenceId or externalReference");
        }
        PilotEvidence item = evidence.save(new PilotEvidence(
                UUID.randomUUID(),
                pilot.id(),
                request.evidenceType(),
                request.referenceType(),
                request.referenceId(),
                blankToNull(request.externalReference()),
                request.summary(),
                actor.userId(),
                actor.username()));
        audit("pilot.evidence-added", pilot, actor, Map.of(
                "evidenceType", item.evidenceType().name(),
                "referenceType", item.referenceType()));
        return PilotEvidenceResponse.from(item);
    }

    @Transactional
    public PilotOperationalGateResponse addOperationalGate(
            UUID pilotId,
            CreatePilotOperationalGateRequest request,
            AuthenticatedActor actor) {
        PilotReadinessRecord pilot = requirePilot(pilotId);
        PilotOperationalGate gate = operationalGates.save(new PilotOperationalGate(
                UUID.randomUUID(),
                pilot.id(),
                request.gateType(),
                request.ownerRole(),
                request.summary(),
                blankToNull(request.evidenceReference()),
                actor.userId(),
                actor.username()));
        audit("pilot.operational-gate-created", pilot, actor, Map.of(
                "gateType", gate.gateType().name(),
                "ownerRole", gate.ownerRole()));
        return PilotOperationalGateResponse.from(gate);
    }

    @Transactional
    public PilotOperationalGateResponse decideOperationalGate(
            UUID pilotId,
            UUID gateId,
            DecidePilotOperationalGateRequest request,
            AuthenticatedActor actor) {
        PilotReadinessRecord pilot = requirePilot(pilotId);
        PilotOperationalGate gate = operationalGates.findById(gateId)
                .filter(candidate -> candidate.pilotId().equals(pilotId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pilot operational gate not found"));
        if (request.status() == PilotOperationalGateStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gate decision cannot remain PENDING");
        }
        gate.decide(request.status(), request.evidenceReference(), actor.userId(), actor.username());
        audit("pilot.operational-gate-decided", pilot, actor, Map.of(
                "gateType", gate.gateType().name(),
                "status", gate.status().name()));
        return PilotOperationalGateResponse.from(gate);
    }

    @Transactional
    public PilotWorkflowResponse requestGoNoGo(UUID pilotId, AuthenticatedActor actor) {
        PilotReadinessRecord pilot = requirePilot(pilotId);
        requirePilotReadyForGoReview(pilot.id());
        UUID taskId = workflowTasks.openPilotGoNoGoTask(pilot.id(), actor);
        pilot.markUnderReview(taskId);
        audit("pilot.go-no-go-requested", pilot, actor, Map.of("workflowTaskId", taskId.toString()));
        return new PilotWorkflowResponse(summary(pilot), "APPROVE_PILOT_GO", "OPEN", taskId);
    }

    @Transactional
    public PilotWorkflowResponse decideGoNoGoTask(
            UUID taskId,
            DecideWorkflowTaskRequest request,
            AuthenticatedActor actor) {
        WorkflowTask existing = workflowTasks.getTask(taskId);
        requireTask(existing, GO_NO_GO_WORKFLOW, GO_NO_GO_TARGET);
        WorkflowTask task = workflowTasks.decideTask(taskId, request, actor);
        PilotReadinessRecord pilot = requirePilot(task.targetId());
        if (request.decision() == WorkflowDecision.APPROVE) {
            requirePilotReadyForGoReview(pilot.id());
            pilot.approveGo(request.reason(), actor.userId(), actor.username());
            audit("pilot.go-approved", pilot, actor, Map.of("workflowTaskId", taskId.toString()));
        } else {
            pilot.rejectGo(request.reason(), actor.userId(), actor.username());
            audit("pilot.no-go-recorded", pilot, actor, Map.of("workflowTaskId", taskId.toString()));
        }
        return new PilotWorkflowResponse(summary(pilot), task.requestedAction(), task.status().name(), task.id());
    }

    private void requirePilotReadyForGoReview(UUID pilotId) {
        List<PilotSignoff> pilotSignoffs = signoffs.findByPilotIdOrderByCreatedAtAsc(pilotId);
        Set<PilotSignoffType> approvedTypes = pilotSignoffs.stream()
                .filter(signoff -> signoff.status() == PilotSignoffStatus.APPROVED)
                .map(PilotSignoff::signoffType)
                .collect(Collectors.toSet());
        long pending = signoffs.countByPilotIdAndStatus(pilotId, PilotSignoffStatus.PENDING);
        long rejected = signoffs.countByPilotIdAndStatus(pilotId, PilotSignoffStatus.REJECTED);
        long openBlockingRisks = risks.countByPilotIdAndBlockingGoLiveTrueAndStatus(pilotId, PilotRiskStatus.OPEN);
        Set<PilotOperationalGateType> acceptedGates = operationalGates.findByPilotIdOrderByCreatedAtAsc(pilotId).stream()
                .filter(gate -> gate.status() == PilotOperationalGateStatus.PASSED
                        || gate.status() == PilotOperationalGateStatus.WAIVED)
                .map(PilotOperationalGate::gateType)
                .collect(Collectors.toSet());
        if (!approvedTypes.containsAll(Set.of(PilotSignoffType.values()))
                || !acceptedGates.containsAll(Set.of(PilotOperationalGateType.values()))
                || pending > 0
                || rejected > 0
                || openBlockingRisks > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Pilot cannot request GO until every required signoff and operational gate is accepted and no blocking risks remain open");
        }
    }

    private PilotReadinessRecord requirePilot(UUID pilotId) {
        return pilots.findById(pilotId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pilot readiness record not found"));
    }

    private PilotReadinessResponse summary(PilotReadinessRecord record) {
        return PilotReadinessResponse.from(
                record,
                signoffs.findByPilotIdOrderByCreatedAtAsc(record.id()),
                risks.findByPilotIdOrderByCreatedAtAsc(record.id()),
                evidence.findByPilotIdOrderByAddedAtDesc(record.id()),
                operationalGates.findByPilotIdOrderByCreatedAtAsc(record.id()));
    }

    private void requireTask(WorkflowTask task, String workflowType, String targetType) {
        if (!workflowType.equals(task.workflowType()) || !targetType.equals(task.targetType())) {
            throw new UnsupportedWorkflowTaskException(task.id());
        }
    }

    private void audit(String action, PilotReadinessRecord pilot, AuthenticatedActor actor, Map<String, Object> details) {
        auditService.record(
                action,
                "pilot-readiness-record",
                pilot.id().toString(),
                AuditClassification.STAFF_OPERATIONAL,
                actor.userId(),
                null,
                details);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
