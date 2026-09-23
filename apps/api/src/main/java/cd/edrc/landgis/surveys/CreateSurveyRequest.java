package cd.edrc.landgis.surveys;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateSurveyRequest(
        @NotNull UUID parcelId,
        UUID assignedToUserId,
        @Size(max = 500) String purpose) {
}
