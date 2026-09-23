package cd.edrc.landgis.transactions;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateParcelInformationApplicationRequest(
        @NotNull UUID parcelId,
        @NotBlank @Size(max = 1000) String purpose) {
}
