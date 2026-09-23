package cd.edrc.landgis.parcels;

import cd.edrc.landgis.administration.AdministrativeUnitRepository;
import cd.edrc.landgis.administration.AdministrativeUnit;
import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import cd.edrc.landgis.workflow.DecideWorkflowTaskRequest;
import cd.edrc.landgis.workflow.UnsupportedWorkflowTaskException;
import cd.edrc.landgis.workflow.WorkflowDecision;
import cd.edrc.landgis.workflow.WorkflowTask;
import cd.edrc.landgis.workflow.WorkflowTaskService;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class ParcelRegistryService {
    private static final String DEFAULT_UPI_SCHEME = "PROPOSED-EDRC";

    private final ParcelRepository parcels;
    private final ParcelIdentifierRepository identifiers;
    private final UpiSchemeVersionRepository upiSchemes;
    private final AdministrativeUnitRepository administrativeUnits;
    private final PublicParcelProjectionService publicParcelProjection;
    private final ParcelStateTransitionPolicy transitionPolicy;
    private final WorkflowTaskService workflowTasks;
    private final AuditService auditService;

    ParcelRegistryService(
            ParcelRepository parcels,
            ParcelIdentifierRepository identifiers,
            UpiSchemeVersionRepository upiSchemes,
            AdministrativeUnitRepository administrativeUnits,
            PublicParcelProjectionService publicParcelProjection,
            ParcelStateTransitionPolicy transitionPolicy,
            WorkflowTaskService workflowTasks,
            AuditService auditService) {
        this.parcels = parcels;
        this.identifiers = identifiers;
        this.upiSchemes = upiSchemes;
        this.administrativeUnits = administrativeUnits;
        this.publicParcelProjection = publicParcelProjection;
        this.transitionPolicy = transitionPolicy;
        this.workflowTasks = workflowTasks;
        this.auditService = auditService;
    }

    @Transactional
    ParcelResponse createDraft(CreateParcelRequest request) {
        AdministrativeUnit administrativeUnit = administrativeUnits.findById(request.administrativeUnitId())
                .orElseThrow(() -> new UnknownAdministrativeUnitException(request.administrativeUnitId()));

        String normalizedUpi = request.proposedUpi().trim().toUpperCase(Locale.ROOT);
        identifiers.findByIdentifierTypeAndIdentifierValueAndStatus(
                        IdentifierType.PROPOSED_UPI,
                        normalizedUpi,
                        IdentifierStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new DuplicateUpiException(normalizedUpi);
                });

        UpiSchemeVersion scheme = upiSchemes.findFirstBySchemeCodeAndStatusOrderByEffectiveFromDesc(DEFAULT_UPI_SCHEME, "ACTIVE")
                .orElseThrow(MissingUpiSchemeException::new);

        Parcel parcel = parcels.save(new Parcel(
                UUID.randomUUID(),
                request.administrativeUnitId(),
                request.landUse(),
                request.tenureClassification()));
        ParcelIdentifier identifier = identifiers.save(new ParcelIdentifier(UUID.randomUUID(), parcel.id(), normalizedUpi, scheme.id()));
        publicParcelProjection.publishDraftSummary(parcel, identifier, administrativeUnit);
        auditService.record("parcel.draft-created", "parcel", parcel.id().toString(), AuditClassification.STAFF_OPERATIONAL);
        return new ParcelResponse(parcel.id(), parcel.status().name(), parcel.administrativeUnitId(), normalizedUpi);
    }

    @Transactional(readOnly = true)
    List<ParcelResponse> findByAdministrativeUnit(UUID administrativeUnitId) {
        return parcels.findByAdministrativeUnitId(administrativeUnitId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    ParcelResponse getParcel(UUID parcelId) {
        return parcels.findById(parcelId)
                .map(this::toResponse)
                .orElseThrow(() -> new ParcelNotFoundException(parcelId));
    }

    @Transactional(readOnly = true)
    Optional<ParcelResponse> findByProposedUpi(String proposedUpi) {
        if (proposedUpi == null || proposedUpi.isBlank()) {
            return Optional.empty();
        }
        String normalizedUpi = proposedUpi.trim().toUpperCase(Locale.ROOT);
        return identifiers.findByIdentifierTypeAndIdentifierValueAndStatus(
                        IdentifierType.PROPOSED_UPI,
                        normalizedUpi,
                        IdentifierStatus.ACTIVE)
                .flatMap(identifier -> parcels.findById(identifier.parcelId()))
                .map(this::toResponse);
    }

    @Transactional
    ParcelTransitionResponse transitionStatus(UUID parcelId, TransitionParcelStatusRequest request, AuthenticatedActor requestedBy) {
        Parcel parcel = parcels.findById(parcelId).orElseThrow(() -> new ParcelNotFoundException(parcelId));
        ParcelStatus fromStatus = parcel.status();
        ParcelStatus targetStatus = request.targetStatus();
        ParcelTransitionDecision decision = transitionPolicy.decide(fromStatus, targetStatus);

        if (!decision.allowed()) {
            throw new InvalidParcelStateTransitionException(fromStatus, targetStatus);
        }

        if (decision.requiresApproval()) {
            UUID taskId = workflowTasks.openParcelTransitionTask(
                    parcel.id(),
                    fromStatus.name() + "_TO_" + targetStatus.name(),
                    requestedBy);
            auditService.record("parcel.status-transition-requested", "parcel", parcel.id().toString(), AuditClassification.STAFF_OPERATIONAL);
            return new ParcelTransitionResponse(parcel.id(), parcel.status().name(), targetStatus.name(), "PENDING_APPROVAL", taskId);
        }

        parcel.transitionTo(targetStatus);
        auditService.record("parcel.status-transitioned", "parcel", parcel.id().toString(), AuditClassification.STAFF_OPERATIONAL);
        return new ParcelTransitionResponse(parcel.id(), parcel.status().name(), targetStatus.name(), "APPLIED", null);
    }

    @Transactional
    ParcelTransitionResponse decideStatusTransitionTask(UUID taskId, DecideWorkflowTaskRequest request, AuthenticatedActor decidedBy) {
        WorkflowTask task = workflowTasks.decideTask(taskId, request, decidedBy);
        ParcelTransition parsedTransition = parseParcelTransitionTask(task);
        Parcel parcel = parcels.findById(task.targetId()).orElseThrow(() -> new ParcelNotFoundException(task.targetId()));

        if (request.decision() == WorkflowDecision.REJECT) {
            auditService.record("parcel.status-transition-rejected", "parcel", parcel.id().toString(), AuditClassification.STAFF_OPERATIONAL);
            return new ParcelTransitionResponse(
                    parcel.id(),
                    parcel.status().name(),
                    parsedTransition.targetStatus().name(),
                    "REJECTED",
                    task.id());
        }

        if (parcel.status() != parsedTransition.fromStatus()) {
            throw new InvalidParcelStateTransitionException(parcel.status(), parsedTransition.targetStatus());
        }

        parcel.transitionTo(parsedTransition.targetStatus());
        auditService.record("parcel.status-transition-approved", "parcel", parcel.id().toString(), AuditClassification.STAFF_OPERATIONAL);
        return new ParcelTransitionResponse(
                parcel.id(),
                parcel.status().name(),
                parsedTransition.targetStatus().name(),
                "APPROVED_AND_APPLIED",
                task.id());
    }

    private ParcelTransition parseParcelTransitionTask(WorkflowTask task) {
        if (!"PARCEL_STATUS_TRANSITION".equals(task.workflowType()) || !"parcel".equals(task.targetType())) {
            throw new UnsupportedWorkflowTaskException(task.id());
        }
        String[] statuses = task.requestedAction().split("_TO_", 2);
        if (statuses.length != 2) {
            throw new UnsupportedWorkflowTaskException(task.id());
        }
        return new ParcelTransition(ParcelStatus.valueOf(statuses[0]), ParcelStatus.valueOf(statuses[1]));
    }

    private ParcelResponse toResponse(Parcel parcel) {
        String activeUpi = identifiers.findByParcelIdAndIdentifierTypeAndStatus(
                        parcel.id(),
                        IdentifierType.PROPOSED_UPI,
                        IdentifierStatus.ACTIVE)
                .map(ParcelIdentifier::identifierValue)
                .orElse(null);
        return new ParcelResponse(parcel.id(), parcel.status().name(), parcel.administrativeUnitId(), activeUpi);
    }

    private record ParcelTransition(ParcelStatus fromStatus, ParcelStatus targetStatus) {
    }
}
