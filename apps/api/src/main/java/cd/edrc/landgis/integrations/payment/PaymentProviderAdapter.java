package cd.edrc.landgis.integrations.payment;

public interface PaymentProviderAdapter {
    String providerCode();

    PaymentProviderResult initiate(PaymentInitiation initiation);
}
