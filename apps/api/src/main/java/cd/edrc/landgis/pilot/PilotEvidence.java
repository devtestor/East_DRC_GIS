package cd.edrc.landgis.pilot;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "pilot", name = "pilot_evidence")
public class PilotEvidence {
    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID pilotId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PilotEvidenceType evidenceType;

    @Column(nullable = false)
    private String referenceType;

    private UUID referenceId;
    private String externalReference;

    @Column(nullable = false)
    private String summary;

    @Column(nullable = false)
    private UUID addedByUserId;

    @Column(nullable = false)
    private String addedBy;

    @Column(nullable = false)
    private OffsetDateTime addedAt;

    protected PilotEvidence() {
    }

    public PilotEvidence(
            UUID id,
            UUID pilotId,
            PilotEvidenceType evidenceType,
            String referenceType,
            UUID referenceId,
            String externalReference,
            String summary,
            UUID addedByUserId,
            String addedBy) {
        this.id = id;
        this.pilotId = pilotId;
        this.evidenceType = evidenceType;
        this.referenceType = referenceType;
        this.referenceId = referenceId;
        this.externalReference = externalReference;
        this.summary = summary;
        this.addedByUserId = addedByUserId;
        this.addedBy = addedBy;
        this.addedAt = OffsetDateTime.now();
    }

    public UUID id() { return id; }
    public UUID pilotId() { return pilotId; }
    public PilotEvidenceType evidenceType() { return evidenceType; }
    public String referenceType() { return referenceType; }
    public UUID referenceId() { return referenceId; }
    public String externalReference() { return externalReference; }
    public String summary() { return summary; }
    public UUID addedByUserId() { return addedByUserId; }
    public String addedBy() { return addedBy; }
    public OffsetDateTime addedAt() { return addedAt; }
}
