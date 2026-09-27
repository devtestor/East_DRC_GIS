package cd.edrc.landgis.governance;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record DocumentExportRequestResponse(
        UUID id,
        UUID documentId,
        String purpose,
        boolean redactionPlanned,
        String status,
        boolean redactionRequired,
        boolean approvalRequired,
        List<String> blockers,
        UUID workflowTaskId,
        UUID requestedByUserId,
        String requestedBy,
        OffsetDateTime requestedAt,
        UUID decidedByUserId,
        String decidedBy,
        OffsetDateTime decidedAt,
        String decisionReason) {
    static DocumentExportRequestResponse from(DocumentExportRequest request) {
        return new DocumentExportRequestResponse(
                request.id(),
                request.documentId(),
                request.purpose(),
                request.redactionPlanned(),
                request.status().name(),
                request.redactionRequired(),
                request.approvalRequired(),
                blockers(request.blockerSummary()),
                request.workflowTaskId(),
                request.requestedByUserId(),
                request.requestedBy(),
                request.requestedAt(),
                request.decidedByUserId(),
                request.decidedBy(),
                request.decidedAt(),
                request.decisionReason());
    }

    private static List<String> blockers(String blockerSummary) {
        if (blockerSummary == null || blockerSummary.isBlank()) {
            return List.of();
        }
        return Arrays.stream(blockerSummary.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }
}
