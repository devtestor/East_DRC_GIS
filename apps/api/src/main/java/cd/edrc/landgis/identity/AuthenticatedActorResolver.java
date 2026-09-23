package cd.edrc.landgis.identity;

import cd.edrc.landgis.common.AuthenticatedActor;
import java.util.Locale;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthenticatedActorResolver {
    private final UserAccountRepository users;

    AuthenticatedActorResolver(UserAccountRepository users) {
        this.users = users;
    }

    @Transactional(readOnly = true)
    public AuthenticatedActor requireActor(String username) {
        if (username == null || username.isBlank()) {
            throw new UsernameNotFoundException("Authenticated user is required");
        }
        String normalizedEmail = username.trim().toLowerCase(Locale.ROOT);
        UserAccount account = users.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if (account.status() != UserAccountStatus.ACTIVE) {
            throw new UsernameNotFoundException("User is not active");
        }
        return new AuthenticatedActor(account.id(), account.email());
    }
}
