package cd.edrc.landgis.identity;

import static org.assertj.core.api.Assertions.assertThat;

import cd.edrc.landgis.audit.AuditService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UserRegistrationServiceTest {
    @Test
    void normalizesEmailBeforePersisting() {
        UserAccountRepository users = Mockito.mock(UserAccountRepository.class);
        AuditService audit = Mockito.mock(AuditService.class);
        Mockito.when(users.findByEmail("person@example.test")).thenReturn(Optional.empty());
        UserRegistrationService service = new UserRegistrationService(users, new BCryptPasswordEncoder(4), audit);

        RegisterUserResponse response = service.register(new RegisterUserRequest(
                " Person@Example.test ",
                "Fictional Person",
                "long-local-password"));

        assertThat(response.status()).isEqualTo(UserAccountStatus.PENDING_VERIFICATION.name());
        Mockito.verify(users).findByEmail("person@example.test");
    }
}
