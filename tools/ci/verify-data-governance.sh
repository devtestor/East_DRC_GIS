#!/usr/bin/env bash
set -euo pipefail

required_files=(
  "apps/api/src/main/resources/db/migration/V030__data_governance_retention_holds.sql"
  "apps/api/src/main/java/cd/edrc/landgis/governance/DataGovernanceService.java"
  "apps/api/src/test/java/cd/edrc/landgis/governance/DataGovernanceServiceTest.java"
  "docs/security/data-governance-retention.md"
  "docs/architecture/phase-14-plan.md"
)

for file in "${required_files[@]}"; do
  if [[ ! -s "$file" ]]; then
    echo "Required data-governance asset is missing or empty: $file" >&2
    exit 1
  fi
done

grep -q "CREATE TABLE governance.retention_policies" apps/api/src/main/resources/db/migration/V030__data_governance_retention_holds.sql
grep -q "CREATE TABLE governance.legal_holds" apps/api/src/main/resources/db/migration/V030__data_governance_retention_holds.sql
grep -q "legal_holds_one_active_target_idx" apps/api/src/main/resources/db/migration/V030__data_governance_retention_holds.sql
grep -q "ACTIVE_LEGAL_HOLD" apps/api/src/main/java/cd/edrc/landgis/governance/DataGovernanceService.java
grep -q "REDACTION_REQUIRED" apps/api/src/main/java/cd/edrc/landgis/governance/DataGovernanceService.java
grep -q "blocksAutomatedDispositionWhenDocumentHasActiveLegalHold" apps/api/src/test/java/cd/edrc/landgis/governance/DataGovernanceServiceTest.java
grep -q "blocksSensitiveExportWithoutRedactionAndApproval" apps/api/src/test/java/cd/edrc/landgis/governance/DataGovernanceServiceTest.java
