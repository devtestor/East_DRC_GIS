package cd.edrc.landgis.surveys;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;
import jakarta.validation.constraints.NotBlank;

public record CreateSurveyObservationRequest(
        @NotNull SurveyObservationType observationType,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal accuracyMeters,
        @Size(max = 1000) String note,
        UUID clientObservationId,
        @NotBlank String deviceId,
        String captureSource,
        @Size(max = 40) String gnssFixQuality,
        @Size(max = 500) String photoObjectKey,
        String signatureStatus) {
}
