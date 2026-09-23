package cd.edrc.landgis.identity;

import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@EnableConfigurationProperties(DevelopmentSeedProperties.class)
class DevelopmentIdentitySeed implements ApplicationRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevelopmentIdentitySeed.class);

    private final DevelopmentSeedProperties properties;
    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    DevelopmentIdentitySeed(
            DevelopmentSeedProperties properties,
            UserAccountRepository users,
            PasswordEncoder passwordEncoder,
            JdbcTemplate jdbcTemplate) {
        this.properties = properties;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            return;
        }

        String email = properties.staffEmail().trim().toLowerCase(Locale.ROOT);
        Optional<UserAccount> existing = users.findByEmail(email);
        UserAccount seed;
        if (existing.isPresent()) {
            seed = existing.get();
        } else {
            seed = new UserAccount(
                    UUID.randomUUID(),
                    email,
                    "Phase 2 Fictional Staff User",
                    passwordEncoder.encode(properties.staffPassword()));
            seed.activateForDevelopmentSeed();
            users.save(seed);
            LOGGER.warn("Created fictional local development staff account: {}", email);
        }

        ensureDevelopmentOrganizationAndMembership(seed.id());
    }

    private void ensureDevelopmentOrganizationAndMembership(UUID userId) {
        UUID organizationId = UUID.fromString("00000000-0000-0000-0000-000000000201");
        jdbcTemplate.update(
                """
                INSERT INTO identity.organizations (id, organization_type, legal_name, status)
                VALUES (?, 'GOVERNMENT_INSTITUTION', 'Fictional Eastern DRC Cadastral Office', 'ACTIVE')
                ON CONFLICT (id) DO NOTHING
                """,
                organizationId);
        ensureActiveMembership(userId, organizationId, "CADASTRAL_OFFICER");
        ensureActiveMembership(userId, organizationId, "LAND_TITLE_OFFICER");
        ensureActiveMembership(userId, organizationId, "SECURITY_OFFICER");
    }

    private void ensureActiveMembership(UUID userId, UUID organizationId, String roleCode) {
        jdbcTemplate.update(
                """
                INSERT INTO identity.organization_memberships (
                    user_id,
                    organization_id,
                    role_id,
                    province_code,
                    jurisdiction_path,
                    status
                )
                SELECT ?, ?, role.id, 'NK-FICTIONAL', 'NK-FICTIONAL', 'ACTIVE'
                FROM identity.roles role
                WHERE role.code = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM identity.organization_memberships membership
                      WHERE membership.user_id = ?
                        AND membership.organization_id = ?
                        AND membership.role_id = role.id
                        AND membership.status = 'ACTIVE'
                  )
                """,
                userId,
                organizationId,
                roleCode,
                userId,
                organizationId);
    }
}
