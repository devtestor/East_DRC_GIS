package cd.edrc.landgis.parcels;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class ParcelStateTransitionPolicy {
    private final JdbcTemplate jdbcTemplate;

    ParcelStateTransitionPolicy(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    ParcelTransitionDecision decide(ParcelStatus fromStatus, ParcelStatus toStatus) {
        return jdbcTemplate.query(
                """
                SELECT requires_approval
                FROM parcels.parcel_state_transitions
                WHERE from_status = ? AND to_status = ?
                """,
                resultSet -> {
                    if (!resultSet.next()) {
                        return ParcelTransitionDecision.denied();
                    }
                    return new ParcelTransitionDecision(true, resultSet.getBoolean("requires_approval"));
                },
                fromStatus.name(),
                toStatus.name());
    }
}
