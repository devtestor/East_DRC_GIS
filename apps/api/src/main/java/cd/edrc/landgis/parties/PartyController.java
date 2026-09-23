package cd.edrc.landgis.parties;

import cd.edrc.landgis.identity.AuthenticatedActorResolver;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/parties")
class PartyController {
    private final PartyService parties;
    private final AuthenticatedActorResolver actors;

    PartyController(PartyService parties, AuthenticatedActorResolver actors) {
        this.parties = parties;
        this.actors = actors;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    PartyResponse create(@Valid @RequestBody CreatePartyRequest request, Principal principal) {
        return parties.create(request, actors.requireActor(principal == null ? null : principal.getName()));
    }

    @GetMapping("/{partyId}")
    PartyResponse get(@PathVariable UUID partyId) {
        return parties.get(partyId);
    }

    @GetMapping
    List<PartyResponse> search(@RequestParam(required = false) String query) {
        return parties.search(query);
    }
}
