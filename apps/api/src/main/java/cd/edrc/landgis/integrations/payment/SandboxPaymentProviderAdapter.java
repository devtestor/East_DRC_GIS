package cd.edrc.landgis.integrations.payment;

import java.util.Objects;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!production")
public class SandboxPaymentProviderAdapter implements PaymentProviderAdapter {
    @Override
    public String providerCode() {
        return "SANDBOX";
    }

    @Override
    public PaymentProviderResult initiate(PaymentInitiation initiation) {
        Objects.requireNonNull(initiation, "initiation");
        return new PaymentProviderResult(providerCode(), "PENDING", "SANDBOX-" + initiation.invoiceId(), true);
    }
}
