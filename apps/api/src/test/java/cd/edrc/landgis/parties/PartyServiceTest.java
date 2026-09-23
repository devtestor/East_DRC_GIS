package cd.edrc.landgis.parties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class PartyServiceTest {
    @Test
    void createsProtectedPartyRecordAndAudits() {
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        AuthenticatedActor actor = new AuthenticatedActor(UUID.randomUUID(), "officer@example.test");
        PartyResponse stored = new PartyResponse(
                UUID.randomUUID(),
                PartyType.INDIVIDUAL.name(),
                "Fictional Claimant",
                DataConfidence.SELF_DECLARED.name(),
                PartyVerificationStatus.PENDING.name(),
                actor.userId(),
                actor.username(),
                OffsetDateTime.now());
        when(jdbc.queryForObject(
                anyString(),
                ArgumentMatchers.<RowMapper<PartyResponse>>any(),
                any(),
                eq(PartyType.INDIVIDUAL.name()),
                eq("Fictional Claimant"),
                eq(DataConfidence.SELF_DECLARED.name()),
                eq(actor.userId()),
                eq(actor.username()))).thenReturn(stored);
        PartyService service = new PartyService(jdbc, audit);

        PartyResponse response = service.create(
                new CreatePartyRequest(PartyType.INDIVIDUAL, " Fictional Claimant ", DataConfidence.SELF_DECLARED),
                actor);

        assertThat(response.verificationStatus()).isEqualTo(PartyVerificationStatus.PENDING.name());
        verify(audit).record(
                eq("party.created"),
                eq("party"),
                eq(response.id().toString()),
                eq(AuditClassification.PROTECTED_PERSONAL),
                eq(actor.userId()),
                eq(null),
                any());
    }

    @Test
    void searchesByDisplayName() {
        JdbcTemplate jdbc = Mockito.mock(JdbcTemplate.class);
        AuditService audit = Mockito.mock(AuditService.class);
        PartyResponse party = new PartyResponse(
                UUID.randomUUID(),
                PartyType.ORGANIZATION.name(),
                "Fictional Cooperative",
                DataConfidence.DOCUMENT_SUPPORTED.name(),
                PartyVerificationStatus.PENDING.name(),
                UUID.randomUUID(),
                "officer@example.test",
                OffsetDateTime.now());
        when(jdbc.query(
                anyString(),
                ArgumentMatchers.<RowMapper<PartyResponse>>any(),
                eq("%cooperative%"))).thenReturn(List.of(party));
        PartyService service = new PartyService(jdbc, audit);

        List<PartyResponse> response = service.search(" Cooperative ");

        assertThat(response).hasSize(1);
        assertThat(response.get(0).displayName()).isEqualTo("Fictional Cooperative");
    }
}
