package cd.edrc.landgis.workflow;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WorkflowTaskEvidenceResponse(
        UUID id,
        UUID taskId,
        String evidenceType,
        String referenceType,
        UUID referenceId,
        String externalReference,
        String summary,
        UUID addedByUserId,
        String addedBy,
        OffsetDateTime addedAt) {
    static WorkflowTaskEvidenceResponse from(WorkflowTaskEvidenceLink evidence) {
        return new WorkflowTaskEvidenceResponse(
                evidence.id(),
                evidence.taskId(),
                evidence.evidenceType().name(),
                evidence.referenceType(),
                evidence.referenceId(),
                evidence.externalReference(),
                evidence.summary(),
                evidence.addedByUserId(),
                evidence.addedBy(),
                evidence.addedAt());
    }
}
