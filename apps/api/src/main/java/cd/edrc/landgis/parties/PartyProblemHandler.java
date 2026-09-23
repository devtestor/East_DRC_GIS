package cd.edrc.landgis.parties;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class PartyProblemHandler {
    @ExceptionHandler(PartyNotFoundException.class)
    ProblemDetail partyNotFound(PartyNotFoundException exception) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setType(URI.create("https://edrc-land-gis.local/problems/party-not-found"));
        problem.setTitle("Party not found");
        return problem;
    }
}
