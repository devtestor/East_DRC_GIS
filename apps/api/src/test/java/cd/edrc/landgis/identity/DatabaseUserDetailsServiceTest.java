package cd.edrc.landgis.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DatabaseUserDetailsServiceTest {
    @Test
    void mapsActiveUserToStaffAuthority() {
        UserAccountRepository users = Mockito.mock(UserAccountRepository.class);
        UserAccount account = new UserAccount(
                UUID.randomUUID(),
                "staff@example.test",
                "Staff",
                "{noop}secret");
        account.activateForDevelopmentSeed();
        when(users.findByEmail("staff@example.test")).thenReturn(Optional.of(account));

        DatabaseUserDetailsService service = new DatabaseUserDetailsService(users);

        assertThat(service.loadUserByUsername(" Staff@Example.Test ").getAuthorities())
                .extracting(Object::toString)
                .contains("ROLE_STAFF");
    }
}
