package cd.edrc.landgis.identity;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edrc.dev-seed")
record DevelopmentSeedProperties(boolean enabled, String staffEmail, String staffPassword) {
}
