#!/usr/bin/env bash
set -euo pipefail

required_files=(
  "apps/api/src/main/resources/db/migration/V030__data_governance_retention_holds.sql"
  "apps/api/src/main/java/cd/edrc/landgis/governance/DataGovernanceService.java"
  "apps/api/src/main/java/cd/edrc/landgis/governance/DocumentExportRequestService.java"
  "apps/api/src/main/java/cd/edrc/landgis/governance/DocumentExportPackageService.java"
  "apps/api/src/test/java/cd/edrc/landgis/governance/DataGovernanceServiceTest.java"
  "apps/api/src/test/java/cd/edrc/landgis/governance/DocumentExportRequestServiceTest.java"
  "apps/api/src/test/java/cd/edrc/landgis/governance/DocumentExportPackageServiceTest.java"
  "apps/api/src/main/resources/db/migration/V031__governed_export_requests.sql"
  "apps/api/src/main/resources/db/migration/V032__governed_export_packages.sql"
  "apps/staff-console/src/app/staff-console-client.tsx"
  "docs/security/data-governance-retention.md"
  "docs/security/privacy-export-workflow.md"
  "docs/architecture/phase-14-plan.md"
  "docs/architecture/phase-15-plan.md"
  "docs/architecture/phase-16-plan.md"
  "docs/architecture/phase-17-plan.md"
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
grep -q "DOCUMENT_EXPORT_REVIEW" apps/api/src/main/java/cd/edrc/landgis/workflow/HighRiskWorkflowPolicy.java
grep -q "CREATE TABLE governance.export_requests" apps/api/src/main/resources/db/migration/V031__governed_export_requests.sql
grep -q "CREATE TABLE governance.export_packages" apps/api/src/main/resources/db/migration/V032__governed_export_packages.sql
grep -q "export_packages_request_unique_idx" apps/api/src/main/resources/db/migration/V032__governed_export_packages.sql
grep -q "token_sha256" apps/api/src/main/resources/db/migration/V032__governed_export_packages.sql
grep -q "APPROVE_DOCUMENT_EXPORT" apps/api/src/main/java/cd/edrc/landgis/workflow/WorkflowTaskService.java
grep -q "document-export-package-generated" apps/api/src/main/java/cd/edrc/landgis/governance/DocumentExportPackageService.java
grep -q "document-export-package-downloaded" apps/api/src/main/java/cd/edrc/landgis/governance/DocumentExportPackageService.java
grep -q "DOCUMENT_EXPORT_REVIEW" apps/staff-console/src/app/staff-console-client.tsx
grep -q "export-requests" apps/staff-console/src/app/staff-console-client.tsx
grep -q "export-packages" apps/staff-console/src/app/staff-console-client.tsx
grep -q "blocksAutomatedDispositionWhenDocumentHasActiveLegalHold" apps/api/src/test/java/cd/edrc/landgis/governance/DataGovernanceServiceTest.java
grep -q "blocksSensitiveExportWithoutRedactionAndApproval" apps/api/src/test/java/cd/edrc/landgis/governance/DataGovernanceServiceTest.java
grep -q "opensSecurityWorkflowWhenSensitiveExportHasRedactionPlan" apps/api/src/test/java/cd/edrc/landgis/governance/DocumentExportRequestServiceTest.java
grep -q "generatesPackageOnlyForApprovedExportRequestAndReturnsTokenOnce" apps/api/src/test/java/cd/edrc/landgis/governance/DocumentExportPackageServiceTest.java
grep -q "downloadsPackageWithUnexpiredTokenAndVerifiesChecksum" apps/api/src/test/java/cd/edrc/landgis/governance/DocumentExportPackageServiceTest.java
