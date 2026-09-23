package cd.edrc.landgis.surveys;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitSurveyRequest(@NotBlank @Size(max = 200_000) String geometryWkt) {
}
