package cd.edrc.landgis.integrations;

import static org.assertj.core.api.Assertions.assertThat;

import cd.edrc.landgis.integrations.identity.IdentityVerificationRequest;
import cd.edrc.landgis.integrations.identity.SandboxIdentityVerificationAdapter;
import cd.edrc.landgis.integrations.payment.PaymentInitiation;
import cd.edrc.landgis.integrations.payment.SandboxPaymentProviderAdapter;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SandboxAdapterTest {
    @Test
    void paymentAdapterNeverClaimsPaymentSuccess() {
        var result = new SandboxPaymentProviderAdapter().initiate(
                new PaymentInitiation(UUID.randomUUID(), new BigDecimal("10.00"), "USD", "callback"));
        assertThat(result.provider()).isEqualTo("SANDBOX");
        assertThat(result.status()).isEqualTo("PENDING");
        assertThat(result.sandbox()).isTrue();
    }

    @Test
    void identityAdapterDoesNotClaimGovernmentVerification() {
        var result = new SandboxIdentityVerificationAdapter().verify(
                new IdentityVerificationRequest("subject-1", "document-1"));
        assertThat(result.status()).isEqualTo("PENDING_HUMAN_REVIEW");
        assertThat(result.sandbox()).isTrue();
    }
}
