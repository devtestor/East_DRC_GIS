package cd.edrc.landgis.rights;

import cd.edrc.landgis.parties.DataConfidence;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateOwnershipInterestRequest(
        @NotNull UUID partyId,
        @NotNull InterestType interestType,
        @DecimalMin(value = "0.01") @DecimalMax(value = "100.00") BigDecimal sharePercent,
        @NotNull DataConfidence dataConfidence,
        @NotBlank @Size(max = 160) String source) {
}
