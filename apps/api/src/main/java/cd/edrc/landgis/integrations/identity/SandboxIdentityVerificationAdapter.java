package cd.edrc.landgis.integrations.identity;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!production")
public class SandboxIdentityVerificationAdapter implements IdentityVerificationAdapter {
    @Override
    public String providerCode() {
        return "SANDBOX";
    }

    @Override
    public IdentityVerificationResult verify(IdentityVerificationRequest request) {
        if (request == null || request.subjectReference() == null || request.subjectReference().isBlank()) {
            return new IdentityVerificationResult(providerCode(), "NOT_VERIFIED", true, "Subject reference is required");
        }
        return new IdentityVerificationResult(providerCode(), "PENDING_HUMAN_REVIEW", true,
                "No government identity service is connected in this environment");
    }
}
