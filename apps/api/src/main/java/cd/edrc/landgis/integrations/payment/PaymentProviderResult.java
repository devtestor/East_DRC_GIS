package cd.edrc.landgis.integrations.payment;

public record PaymentProviderResult(String provider, String status, String providerReference, boolean sandbox) {
}
