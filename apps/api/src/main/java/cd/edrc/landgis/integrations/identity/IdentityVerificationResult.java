package cd.edrc.landgis.integrations.identity;

public record IdentityVerificationResult(String provider, String status, boolean sandbox, String reason) {
}
