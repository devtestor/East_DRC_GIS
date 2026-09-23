package cd.edrc.landgis.audit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.common.CorrelationIdFilter;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class AuditServiceTest {
    @AfterEach
    void clearMdc() {
        MDC.clear();
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void recordsActorAndDataMinimizedEvidence() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuditService service = service(repository, "");
        UUID actorUserId = UUID.randomUUID();
        UUID actorOrganizationId = UUID.randomUUID();
        MDC.put(CorrelationIdFilter.MDC_KEY, "test-correlation-id");

        UUID auditId = service.record(
                "document.metadata-read",
                "document",
                UUID.randomUUID().toString(),
                AuditClassification.PROTECTED_PERSONAL,
                actorUserId,
                actorOrganizationId,
                Map.of(
                        "documentType", "IDENTITY_EVIDENCE",
                        "ownerType", "party",
                        "operation", "read"));

        Mockito.verify(repository).save(eventCaptor.capture());
        AuditEvent event = eventCaptor.getValue();
        assertThat(auditId).isEqualTo(event.id());
        assertThat(event.correlationId()).isEqualTo("test-correlation-id");
        assertThat(event.actorUserId()).isEqualTo(actorUserId);
        assertThat(event.actorOrganizationId()).isEqualTo(actorOrganizationId);
        assertThat(event.action()).isEqualTo("document.metadata-read");
        assertThat(event.evidence())
                .containsEntry("documentType", "IDENTITY_EVIDENCE")
                .containsEntry("ownerType", "party")
                .containsEntry("operation", "read");
    }

    @Test
    void recordsRequestIpAndTruncatedUserAgent() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RegisteredDeviceVerifier devices = mock(RegisteredDeviceVerifier.class);
        when(devices.isActiveRegisteredDevice("FIELD-TABLET-001")).thenReturn(true);
        AuditService service = new AuditService(repository, new AuditRequestMetadataProperties(""), devices);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.24");
        request.addHeader("User-Agent", "a".repeat(350));
        request.addHeader("X-Device-Id", "FIELD-TABLET-001");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        service.record(
                "document.metadata-read",
                "document",
                UUID.randomUUID().toString(),
                AuditClassification.PROTECTED_PERSONAL);

        Mockito.verify(repository).save(eventCaptor.capture());
        AuditEvent event = eventCaptor.getValue();
        assertThat(event.actorIp().getHostAddress()).isEqualTo("192.0.2.24");
        assertThat(event.actorUserAgent()).hasSize(300);
        assertThat(event.sourceDeviceId()).isEqualTo("FIELD-TABLET-001");
    }

    @Test
    void ignoresInvalidRequestIp() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuditService service = service(repository, "");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("not-an-ip-address");
        request.addHeader("User-Agent", "Mozilla/5.0 Fictional");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        service.record(
                "document.metadata-read",
                "document",
                UUID.randomUUID().toString(),
                AuditClassification.PROTECTED_PERSONAL);

        Mockito.verify(repository).save(eventCaptor.capture());
        AuditEvent event = eventCaptor.getValue();
        assertThat(event.actorIp()).isNull();
        assertThat(event.actorUserAgent()).isEqualTo("Mozilla/5.0 Fictional");
    }

    @Test
    void ignoresForwardedForWhenRemoteAddressIsNotTrusted() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuditService service = service(repository, "10.0.0.0/8");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.30");
        request.addHeader("X-Forwarded-For", "198.51.100.10");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        service.record(
                "document.metadata-read",
                "document",
                UUID.randomUUID().toString(),
                AuditClassification.PROTECTED_PERSONAL);

        Mockito.verify(repository).save(eventCaptor.capture());
        AuditEvent event = eventCaptor.getValue();
        assertThat(event.actorIp().getHostAddress()).isEqualTo("192.0.2.30");
    }

    @Test
    void usesForwardedForWhenRemoteAddressIsTrusted() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuditService service = service(repository, "10.0.0.0/8, 2001:db8::/32");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.1.2.3");
        request.addHeader("X-Forwarded-For", "198.51.100.10, 10.1.2.3");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        service.record(
                "document.metadata-read",
                "document",
                UUID.randomUUID().toString(),
                AuditClassification.PROTECTED_PERSONAL);

        Mockito.verify(repository).save(eventCaptor.capture());
        AuditEvent event = eventCaptor.getValue();
        assertThat(event.actorIp().getHostAddress()).isEqualTo("198.51.100.10");
    }

    @Test
    void fallsBackToTrustedRemoteAddressWhenForwardedForIsInvalid() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuditService service = service(repository, "10.0.0.0/8");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.1.2.3");
        request.addHeader("X-Forwarded-For", "not-an-ip-address");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        service.record(
                "document.metadata-read",
                "document",
                UUID.randomUUID().toString(),
                AuditClassification.PROTECTED_PERSONAL);

        Mockito.verify(repository).save(eventCaptor.capture());
        AuditEvent event = eventCaptor.getValue();
        assertThat(event.actorIp().getHostAddress()).isEqualTo("10.1.2.3");
    }

    @Test
    void ignoresInvalidDeviceId() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuditService service = service(repository, "");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.24");
        request.addHeader("X-Device-Id", "../not a valid device id");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        service.record(
                "document.metadata-read",
                "document",
                UUID.randomUUID().toString(),
                AuditClassification.PROTECTED_PERSONAL);

        Mockito.verify(repository).save(eventCaptor.capture());
        AuditEvent event = eventCaptor.getValue();
        assertThat(event.sourceDeviceId()).isNull();
    }

    @Test
    void ignoresOverlongDeviceId() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        AuditService service = service(repository, "");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.24");
        request.addHeader("X-Device-Id", "D".repeat(81));
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        service.record(
                "document.metadata-read",
                "document",
                UUID.randomUUID().toString(),
                AuditClassification.PROTECTED_PERSONAL);

        Mockito.verify(repository).save(eventCaptor.capture());
        AuditEvent event = eventCaptor.getValue();
        assertThat(event.sourceDeviceId()).isNull();
    }

    @Test
    void ignoresUnregisteredDeviceId() {
        AuditEventRepository repository = Mockito.mock(AuditEventRepository.class);
        ArgumentCaptor<AuditEvent> eventCaptor = ArgumentCaptor.forClass(AuditEvent.class);
        when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));
        RegisteredDeviceVerifier devices = mock(RegisteredDeviceVerifier.class);
        when(devices.isActiveRegisteredDevice("FIELD-TABLET-999")).thenReturn(false);
        AuditService service = new AuditService(repository, new AuditRequestMetadataProperties(""), devices);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.24");
        request.addHeader("X-Device-Id", "FIELD-TABLET-999");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        service.record(
                "document.metadata-read",
                "document",
                UUID.randomUUID().toString(),
                AuditClassification.PROTECTED_PERSONAL);

        Mockito.verify(repository).save(eventCaptor.capture());
        AuditEvent event = eventCaptor.getValue();
        assertThat(event.sourceDeviceId()).isNull();
    }

    private AuditService service(AuditEventRepository repository, String trustedProxyCidrs) {
        RegisteredDeviceVerifier devices = mock(RegisteredDeviceVerifier.class);
        return new AuditService(repository, new AuditRequestMetadataProperties(trustedProxyCidrs), devices);
    }
}
