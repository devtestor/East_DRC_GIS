package cd.edrc.landgis.pilot;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record PilotReadinessResponse(
        UUID id,
        String title,
        String geographyScope,
        String pilotOwner,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate,
        String status,
        String finalDecisionReason,
        UUID finalDecisionTaskId,
        String requestedBy,
        String decidedBy,
        OffsetDateTime decidedAt,
        OffsetDateTime createdAt,
        List<PilotSignoffResponse> signoffs,
        List<PilotRiskResponse> risks,
        List<PilotEvidenceResponse> evidence) {
    static PilotReadinessResponse from(
            PilotReadinessRecord record,
            List<PilotSignoff> signoffs,
            List<PilotRisk> risks,
            List<PilotEvidence> evidence) {
        return new PilotReadinessResponse(
                record.id(),
                record.title(),
                record.geographyScope(),
                record.pilotOwner(),
                record.plannedStartDate(),
                record.plannedEndDate(),
                record.status().name(),
                record.finalDecisionReason(),
                record.finalDecisionTaskId(),
                record.requestedBy(),
                record.decidedBy(),
                record.decidedAt(),
                record.createdAt(),
                signoffs.stream().map(PilotSignoffResponse::from).toList(),
                risks.stream().map(PilotRiskResponse::from).toList(),
                evidence.stream().map(PilotEvidenceResponse::from).toList());
    }
}
