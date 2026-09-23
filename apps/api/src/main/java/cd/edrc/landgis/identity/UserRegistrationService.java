package cd.edrc.landgis.identity;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import java.util.Locale;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class UserRegistrationService {
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    UserRegistrationService(
            UserAccountRepository users,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    RegisterUserResponse register(RegisterUserRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        users.findByEmail(normalizedEmail).ifPresent(existing -> {
            throw new DuplicateUserException(normalizedEmail);
        });

        UserAccount user = new UserAccount(
                UUID.randomUUID(),
                normalizedEmail,
                request.displayName().trim(),
                passwordEncoder.encode(request.password()));
        users.save(user);
        auditService.record("identity.user.registered", "identity.user", user.id().toString(), AuditClassification.SECURITY);
        return new RegisterUserResponse(user.id(), UserAccountStatus.PENDING_VERIFICATION.name());
    }
}
