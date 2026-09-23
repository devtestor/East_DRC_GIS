package cd.edrc.landgis.parties;

import cd.edrc.landgis.audit.AuditClassification;
import cd.edrc.landgis.audit.AuditService;
import cd.edrc.landgis.common.AuthenticatedActor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PartyService {
    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;

    public PartyService(JdbcTemplate jdbcTemplate, AuditService auditService) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
    }

    @Transactional
    public PartyResponse create(CreatePartyRequest request, AuthenticatedActor actor) {
        PartyResponse response = jdbcTemplate.queryForObject(
                """
                INSERT INTO parties.parties (
                    id,
                    party_type,
                    display_name,
                    data_confidence,
                    verification_status,
                    created_by_user_id,
                    created_by
                )
                VALUES (?, ?, ?, ?, 'PENDING', ?, ?)
                RETURNING id, party_type, display_name, data_confidence, verification_status, created_by_user_id, created_by, created_at
                """,
                mapper(),
                UUID.randomUUID(),
                request.partyType().name(),
                request.displayName().trim(),
                request.dataConfidence().name(),
                actor.userId(),
                actor.username());
        auditService.record(
                "party.created",
                "party",
                response.id().toString(),
                AuditClassification.PROTECTED_PERSONAL,
                actor.userId(),
                null,
                java.util.Map.of(
                        "partyType", response.partyType(),
                        "dataConfidence", response.dataConfidence(),
                        "verificationStatus", response.verificationStatus()));
        return response;
    }

    @Transactional(readOnly = true)
    public PartyResponse get(UUID partyId) {
        return find(partyId).orElseThrow(() -> new PartyNotFoundException(partyId));
    }

    @Transactional(readOnly = true)
    public Optional<PartyResponse> find(UUID partyId) {
        return jdbcTemplate.query(
                """
                SELECT id, party_type, display_name, data_confidence, verification_status, created_by_user_id, created_by, created_at
                FROM parties.parties
                WHERE id = ?
                """,
                mapper(),
                partyId).stream().findFirst();
    }

    @Transactional(readOnly = true)
    public List<PartyResponse> search(String query) {
        String normalized = query == null || query.isBlank() ? "%" : "%" + query.trim().toLowerCase(java.util.Locale.ROOT) + "%";
        return jdbcTemplate.query(
                """
                SELECT id, party_type, display_name, data_confidence, verification_status, created_by_user_id, created_by, created_at
                FROM parties.parties
                WHERE lower(display_name) LIKE ?
                ORDER BY created_at DESC
                LIMIT 50
                """,
                mapper(),
                normalized);
    }

    private RowMapper<PartyResponse> mapper() {
        return PartyService::map;
    }

    private static PartyResponse map(ResultSet resultSet, int rowNum) throws SQLException {
        return new PartyResponse(
                resultSet.getObject("id", UUID.class),
                resultSet.getString("party_type"),
                resultSet.getString("display_name"),
                resultSet.getString("data_confidence"),
                resultSet.getString("verification_status"),
                resultSet.getObject("created_by_user_id", UUID.class),
                resultSet.getString("created_by"),
                resultSet.getObject("created_at", java.time.OffsetDateTime.class));
    }
}
