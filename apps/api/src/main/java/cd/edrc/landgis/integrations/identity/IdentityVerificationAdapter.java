package cd.edrc.landgis.integrations.identity;

public interface IdentityVerificationAdapter {
    String providerCode();

    IdentityVerificationResult verify(IdentityVerificationRequest request);
}
