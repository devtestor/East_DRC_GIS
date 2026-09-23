package cd.edrc.landgis.pilot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreatePilotReadinessRequest(
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 300) String geographyScope,
        @NotBlank @Size(max = 180) String pilotOwner,
        LocalDate plannedStartDate,
        LocalDate plannedEndDate) {
}
