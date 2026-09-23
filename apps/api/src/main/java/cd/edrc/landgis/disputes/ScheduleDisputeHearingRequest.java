package cd.edrc.landgis.disputes;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;

public record ScheduleDisputeHearingRequest(
        @NotNull @Future OffsetDateTime scheduledFor,
        @NotBlank @Size(max = 300) String venue,
        @Size(max = 2000) String notes) {
}
