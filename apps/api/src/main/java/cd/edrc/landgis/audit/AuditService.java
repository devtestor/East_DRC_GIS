package cd.edrc.landgis.audit;

import cd.edrc.landgis.common.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@EnableConfigurationProperties(AuditRequestMetadataProperties.class)
public class AuditService {
    private static final int MAX_USER_AGENT_LENGTH = 300;
    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String DEVICE_ID_HEADER = "X-Device-Id";
    private static final int MAX_DEVICE_ID_LENGTH = 80;
    private static final java.util.regex.Pattern DEVICE_ID_PATTERN =
            java.util.regex.Pattern.compile("^[A-Za-z0-9][A-Za-z0-9._:-]{2,79}$");

    private final AuditEventRepository auditEvents;
    private final AuditRequestMetadataProperties properties;
    private final RegisteredDeviceVerifier registeredDevices;

    AuditService(
            AuditEventRepository auditEvents,
            AuditRequestMetadataProperties properties,
            RegisteredDeviceVerifier registeredDevices) {
        this.auditEvents = auditEvents;
        this.properties = properties;
        this.registeredDevices = registeredDevices;
    }

    @Transactional
    public UUID record(String action, String targetType, String targetId, AuditClassification classification) {
        return record(action, targetType, targetId, classification, null, null, Map.of());
    }

    @Transactional
    public UUID record(
            String action,
            String targetType,
            String targetId,
            AuditClassification classification,
            UUID actorUserId,
            UUID actorOrganizationId,
            Map<String, Object> evidence) {
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        HttpServletRequest request = currentRequest();
        AuditEvent event = new AuditEvent(
                UUID.randomUUID(),
                OffsetDateTime.now(),
                correlationId == null ? "system" : correlationId,
                actorUserId,
                actorOrganizationId,
                actorIp(request),
                actorUserAgent(request),
                action,
                targetType,
                targetId,
                evidence == null ? Map.of() : Map.copyOf(evidence),
                "apps/api",
                sourceDeviceId(request),
                classification);
        return auditEvents.save(event).id();
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }

    private InetAddress actorIp(HttpServletRequest request) {
        if (request == null || request.getRemoteAddr() == null || request.getRemoteAddr().isBlank()) {
            return null;
        }
        InetAddress remoteAddress = parseAddress(request.getRemoteAddr());
        if (remoteAddress == null) {
            return null;
        }
        if (isTrustedProxy(remoteAddress)) {
            InetAddress forwardedAddress = firstForwardedAddress(request);
            if (forwardedAddress != null) {
                return forwardedAddress;
            }
        }
        return remoteAddress;
    }

    private InetAddress firstForwardedAddress(HttpServletRequest request) {
        String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return null;
        }
        return Arrays.stream(forwardedFor.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(this::parseAddress)
                .filter(address -> address != null)
                .findFirst()
                .orElse(null);
    }

    private InetAddress parseAddress(String value) {
        try {
            return InetAddress.getByName(value);
        } catch (UnknownHostException exception) {
            return null;
        }
    }

    private boolean isTrustedProxy(InetAddress address) {
        if (properties.trustedProxyCidrs().isBlank()) {
            return false;
        }
        return Arrays.stream(properties.trustedProxyCidrs().split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .anyMatch(cidr -> matchesCidr(address, cidr));
    }

    private boolean matchesCidr(InetAddress address, String cidr) {
        String[] parts = cidr.split("/", -1);
        InetAddress network = parseAddress(parts[0]);
        if (network == null || network.getAddress().length != address.getAddress().length) {
            return false;
        }
        int prefixLength = network.getAddress().length * 8;
        if (parts.length == 2) {
            try {
                prefixLength = Integer.parseInt(parts[1]);
            } catch (NumberFormatException exception) {
                return false;
            }
        } else if (parts.length > 2) {
            return false;
        }
        int maxPrefixLength = network.getAddress().length * 8;
        if (prefixLength < 0 || prefixLength > maxPrefixLength) {
            return false;
        }

        BigInteger mask = BigInteger.ONE
                .shiftLeft(maxPrefixLength)
                .subtract(BigInteger.ONE)
                .shiftRight(prefixLength)
                .not()
                .and(BigInteger.ONE.shiftLeft(maxPrefixLength).subtract(BigInteger.ONE));
        BigInteger addressValue = new BigInteger(1, address.getAddress());
        BigInteger networkValue = new BigInteger(1, network.getAddress());
        return addressValue.and(mask).equals(networkValue.and(mask));
    }

    private String actorUserAgent(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String userAgent = request.getHeader("User-Agent");
        if (userAgent == null || userAgent.isBlank()) {
            return null;
        }
        String trimmed = userAgent.trim();
        if (trimmed.length() <= MAX_USER_AGENT_LENGTH) {
            return trimmed;
        }
        return trimmed.substring(0, MAX_USER_AGENT_LENGTH);
    }

    private String sourceDeviceId(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String deviceId = request.getHeader(DEVICE_ID_HEADER);
        if (deviceId == null || deviceId.isBlank()) {
            return null;
        }
        String trimmed = deviceId.trim();
        if (trimmed.length() > MAX_DEVICE_ID_LENGTH || !DEVICE_ID_PATTERN.matcher(trimmed).matches()) {
            return null;
        }
        return registeredDevices.isActiveRegisteredDevice(trimmed) ? trimmed : null;
    }
}
