package cd.edrc.landgis.surveys;

import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import cd.edrc.landgis.parcels.ParcelGeometryVersionResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/surveys")
class SurveyController {
    private final SurveyService surveys;
    private final AuthenticatedActorResolver actors;

    SurveyController(SurveyService surveys, AuthenticatedActorResolver actors) {
        this.surveys = surveys;
        this.actors = actors;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    SurveyResponse create(@Valid @RequestBody CreateSurveyRequest request, Principal principal) {
        return surveys.create(request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping
    List<SurveyResponse> list(Principal principal) {
        return surveys.list(actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{surveyId}/observations")
    SurveyResponse addObservation(@PathVariable UUID surveyId, @Valid @RequestBody CreateSurveyObservationRequest request, Principal principal) {
        return surveys.addObservation(surveyId, request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{surveyId}/submit")
    ParcelGeometryVersionResponse submit(@PathVariable UUID surveyId, @Valid @RequestBody SubmitSurveyRequest request, Principal principal) {
        return surveys.submit(surveyId, request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @PostMapping("/{surveyId}/boundary-acknowledgements")
    BoundaryAcknowledgementResponse acknowledgeBoundary(@PathVariable UUID surveyId,
            @Valid @RequestBody BoundaryAcknowledgementRequest request, Principal principal) {
        return surveys.acknowledgeBoundary(surveyId, request,
                actors.requireActor(principal == null ? null : principal.getName()));
    }
}
