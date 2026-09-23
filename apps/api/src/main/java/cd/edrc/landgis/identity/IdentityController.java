package cd.edrc.landgis.identity;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity")
class IdentityController {
    private final UserRegistrationService registrationService;

    IdentityController(UserRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    RegisterUserResponse register(@Valid @RequestBody RegisterUserRequest request) {
        return registrationService.register(request);
    }
}
