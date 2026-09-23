package cd.edrc.landgis.governance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "governance", name = "retention_policies")
public class RetentionPolicy {
    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String retentionCategory;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private int minimumRetentionDays;

    @Column(nullable = false)
    private boolean archivalRequired;

    @Column(nullable = false)
    private boolean disposalRequiresApproval;

    @Column(nullable = false)
    private boolean protectedFromAutomatedDisposal;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Version
    private long version;

    protected RetentionPolicy() {
    }

    public RetentionPolicy(
            UUID id,
            String retentionCategory,
            String description,
            int minimumRetentionDays,
            boolean archivalRequired,
            boolean disposalRequiresApproval,
            boolean protectedFromAutomatedDisposal) {
        this.id = id;
        this.retentionCategory = retentionCategory;
        this.description = description;
        this.minimumRetentionDays = minimumRetentionDays;
        this.archivalRequired = archivalRequired;
        this.disposalRequiresApproval = disposalRequiresApproval;
        this.protectedFromAutomatedDisposal = protectedFromAutomatedDisposal;
        this.createdAt = OffsetDateTime.now();
    }

    public UUID id() {
        return id;
    }

    public String retentionCategory() {
        return retentionCategory;
    }

    public String description() {
        return description;
    }

    public int minimumRetentionDays() {
        return minimumRetentionDays;
    }

    public boolean archivalRequired() {
        return archivalRequired;
    }

    public boolean disposalRequiresApproval() {
        return disposalRequiresApproval;
    }

    public boolean protectedFromAutomatedDisposal() {
        return protectedFromAutomatedDisposal;
    }
}
