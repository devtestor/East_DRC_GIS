package cd.edrc.landgis.identity;

import java.util.List;
import java.util.Locale;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
class DatabaseUserDetailsService implements UserDetailsService {
    private final UserAccountRepository users;

    DatabaseUserDetailsService(UserAccountRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String normalizedEmail = username.trim().toLowerCase(Locale.ROOT);
        UserAccount account = users.findByEmail(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        boolean active = account.status() == UserAccountStatus.ACTIVE;
        return new User(
                account.email(),
                account.passwordHash(),
                active,
                true,
                true,
                active,
                List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
    }
}
