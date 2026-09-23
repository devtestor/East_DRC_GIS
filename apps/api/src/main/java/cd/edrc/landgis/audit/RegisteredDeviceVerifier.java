package cd.edrc.landgis.audit;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisteredDeviceVerifier {
    private final JdbcTemplate jdbcTemplate;

    RegisteredDeviceVerifier(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional(readOnly = true)
    public boolean isActiveRegisteredDevice(String deviceId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM identity.registered_devices
                WHERE device_id = ?
                  AND status = 'ACTIVE'
                  AND enrolled_at <= now()
                  AND (expires_at IS NULL OR expires_at > now())
                  AND revoked_at IS NULL
                """,
                Integer.class,
                deviceId);
        return count != null && count > 0;
    }
}
