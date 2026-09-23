package cd.edrc.landgis.parcels;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import cd.edrc.landgis.config.SecurityConfig;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PublicParcelController.class)
@Import(SecurityConfig.class)
class PublicParcelControllerSecurityTest {
    @Autowired
    private MockMvc mvc;

    @MockBean
    private PublicParcelSearchService publicParcelSearchService;

    @Test
    void allowsUnauthenticatedPublicParcelSearch() throws Exception {
        when(publicParcelSearchService.findByProposedUpi("NK-DEM-000004"))
                .thenReturn(Optional.of(new PublicParcelSummary(
                        UUID.randomUUID(),
                        "DRAFT",
                        "NK-DEM-000004",
                        UUID.randomUUID(),
                        "Commune fictive publique",
                        "COMMUNE",
                        "Residential",
                        "Customary claim - unverified")));

        mvc.perform(get("/api/v1/public/parcels/search").param("upi", "NK-DEM-000004"))
                .andExpect(status().isOk());
    }
}
