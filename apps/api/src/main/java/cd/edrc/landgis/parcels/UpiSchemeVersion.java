package cd.edrc.landgis.parcels;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(schema = "parcels", name = "upi_scheme_versions")
public class UpiSchemeVersion {
    @Id
    private UUID id;

    @Column(nullable = false)
    private String schemeCode;

    @Column(nullable = false)
    private String versionLabel;

    @Column(nullable = false)
    private String pattern;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private LocalDate effectiveFrom;

    protected UpiSchemeVersion() {
    }

    public UUID id() {
        return id;
    }
}
