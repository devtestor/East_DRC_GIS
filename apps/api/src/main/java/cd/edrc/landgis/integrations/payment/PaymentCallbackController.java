package cd.edrc.landgis.integrations.payment;

import cd.edrc.landgis.identity.AuthenticatedActorResolver;
import jakarta.validation.Valid;
import java.security.Principal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/integrations/payments")
class PaymentCallbackController {
    private final PaymentCallbackService callbacks;
    private final AuthenticatedActorResolver actors;

    PaymentCallbackController(PaymentCallbackService callbacks, AuthenticatedActorResolver actors) {
        this.callbacks = callbacks;
        this.actors = actors;
    }

    @PostMapping("/{provider}/callbacks")
    PaymentCallbackResponse receive(@PathVariable String provider, @Valid @RequestBody PaymentCallbackRequest request, Principal principal) {
        return callbacks.receive(provider, request, actors.requireActor(principal == null ? null : principal.getName()));
    }
}
