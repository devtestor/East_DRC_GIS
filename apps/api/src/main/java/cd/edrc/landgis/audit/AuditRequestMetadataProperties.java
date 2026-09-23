package cd.edrc.landgis.audit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "edrc.audit")
record AuditRequestMetadataProperties(String trustedProxyCidrs) {
    AuditRequestMetadataProperties {
        trustedProxyCidrs = trustedProxyCidrs == null ? "" : trustedProxyCidrs;
    }
}
