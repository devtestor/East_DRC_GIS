package cd.edrc.landgis.administration;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

record CreateAdministrativeUnitRequest(
        UUID parentId,
        @NotNull AdministrativeUnitType unitType,
        @NotBlank @Pattern(regexp = "[A-Z0-9.-]{2,64}") String code,
        @NotBlank @Size(max = 180) String name,
        @NotNull LocalDate validFrom) {
}
