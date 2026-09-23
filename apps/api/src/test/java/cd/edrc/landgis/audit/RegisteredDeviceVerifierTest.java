package cd.edrc.landgis.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;

class RegisteredDeviceVerifierTest {
    @Test
    void activeRegisteredDeviceIsTrusted() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        RegisteredDeviceVerifier verifier = new RegisteredDeviceVerifier(jdbcTemplate);
        when(jdbcTemplate.queryForObject(Mockito.any(String.class), eq(Integer.class), eq("FIELD-TABLET-001")))
                .thenReturn(1);

        assertThat(verifier.isActiveRegisteredDevice("FIELD-TABLET-001")).isTrue();
    }

    @Test
    void revokedOrMissingDeviceIsNotTrusted() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        RegisteredDeviceVerifier verifier = new RegisteredDeviceVerifier(jdbcTemplate);
        when(jdbcTemplate.queryForObject(Mockito.any(String.class), eq(Integer.class), eq("FIELD-TABLET-002")))
                .thenReturn(0);

        assertThat(verifier.isActiveRegisteredDevice("FIELD-TABLET-002")).isFalse();
    }
}
