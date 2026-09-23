package cd.edrc.landgis.parcels;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.JdbcTemplate;

class ParcelStateTransitionPolicyTest {
    @Test
    void returnsApprovalRequirementFromDatabaseTable() {
        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class);
        when(jdbcTemplate.query(
                Mockito.anyString(),
                Mockito.<ResultSetExtractor<ParcelTransitionDecision>>any(),
                Mockito.eq("DRAFT"),
                Mockito.eq("UNDER_SURVEY")))
                .thenAnswer(invocation -> {
                    ResultSet resultSet = Mockito.mock(ResultSet.class);
                    when(resultSet.next()).thenReturn(true);
                    when(resultSet.getBoolean("requires_approval")).thenReturn(false);
                    return invocation.<org.springframework.jdbc.core.ResultSetExtractor<ParcelTransitionDecision>>getArgument(1)
                            .extractData(resultSet);
                });

        ParcelStateTransitionPolicy policy = new ParcelStateTransitionPolicy(jdbcTemplate);

        assertThat(policy.decide(ParcelStatus.DRAFT, ParcelStatus.UNDER_SURVEY))
                .isEqualTo(new ParcelTransitionDecision(true, false));
    }
}
