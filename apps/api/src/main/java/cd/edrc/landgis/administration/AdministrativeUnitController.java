package cd.edrc.landgis.administration;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/administrative-units")
class AdministrativeUnitController {
    private final AdministrativeUnitService administrativeUnitService;

    AdministrativeUnitController(AdministrativeUnitService administrativeUnitService) {
        this.administrativeUnitService = administrativeUnitService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AdministrativeUnitResponse create(@Valid @RequestBody CreateAdministrativeUnitRequest request) {
        return administrativeUnitService.create(request);
    }

    @GetMapping
    List<AdministrativeUnitResponse> findActive() {
        return administrativeUnitService.findActive();
    }
}
