package cd.edrc.landgis.audit;

public class RegisteredDeviceNotFoundException extends RuntimeException {
    public RegisteredDeviceNotFoundException(String deviceId) {
        super("Registered device not found: " + deviceId);
    }
}
