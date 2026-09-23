package cd.edrc.landgis.transactions;

import jakarta.validation.constraints.NotBlank;

public record CorrectionRequest(@NotBlank String reason) {
}
