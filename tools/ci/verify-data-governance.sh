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
  "apps/api/src/main/resources/db/migration/V033__document_version_quarantine.sql"
  "apps/staff-console/src/app/staff-console-client.tsx"
  "docs/security/data-governance-retention.md"
  "docs/security/privacy-export-workflow.md"
  "docs/architecture/phase-14-plan.md"
  "docs/architecture/phase-15-plan.md"
  "docs/architecture/phase-16-plan.md"
  "docs/architecture/phase-17-plan.md"
  "docs/architecture/phase-18-plan.md"
  "docs/architecture/phase-19-plan.md"
  "docs/architecture/phase-20-plan.md"
  "docs/architecture/phase-21-plan.md"
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
grep -q "safety_status" apps/api/src/main/resources/db/migration/V033__document_version_quarantine.sql
grep -q "document_version_safety_review_complete_check" apps/api/src/main/resources/db/migration/V033__document_version_quarantine.sql
grep -q "APPROVE_DOCUMENT_EXPORT" apps/api/src/main/java/cd/edrc/landgis/workflow/WorkflowTaskService.java
grep -q "document-export-package-generated" apps/api/src/main/java/cd/edrc/landgis/governance/DocumentExportPackageService.java
grep -q "document-export-package-downloaded" apps/api/src/main/java/cd/edrc/landgis/governance/DocumentExportPackageService.java
grep -q "DOCUMENT_EXPORT_REVIEW" apps/staff-console/src/app/staff-console-client.tsx
grep -q "export-requests" apps/staff-console/src/app/staff-console-client.tsx
grep -q "export-packages" apps/staff-console/src/app/staff-console-client.tsx
grep -q "supportedLanguages" apps/staff-console/src/app/staff-console-client.tsx
grep -q "English" apps/staff-console/src/app/staff-console-i18n.ts
grep -q "Kiswahili" apps/staff-console/src/app/staff-console-i18n.ts
grep -q "documentIntake" apps/staff-console/src/app/staff-console-i18n.ts
grep -q "referenceId" apps/staff-console/src/app/staff-console-client.tsx
grep -q "Synthese de gouvernance documentaire" apps/staff-console/src/app/staff-console-client.tsx
grep -q "Revue scan/signature de version" apps/staff-console/src/app/staff-console-client.tsx
grep -q "Quarantaine / liberation de version" apps/staff-console/src/app/staff-console-client.tsx
grep -q "safety-status" apps/api/src/main/java/cd/edrc/landgis/documents/DocumentController.java
grep -q "quarantine" apps/api/src/main/java/cd/edrc/landgis/documents/DocumentController.java
grep -q "release" apps/api/src/main/java/cd/edrc/landgis/documents/DocumentController.java
grep -q "document.version-safety-status-updated" apps/api/src/main/java/cd/edrc/landgis/documents/DocumentService.java
grep -q "document.version-safety-update-denied" apps/api/src/main/java/cd/edrc/landgis/documents/DocumentService.java
grep -q "document.version-quarantined" apps/api/src/main/java/cd/edrc/landgis/documents/DocumentService.java
grep -q "document.version-released" apps/api/src/main/java/cd/edrc/landgis/documents/DocumentService.java
grep -q "document.version-release-blocked" apps/api/src/main/java/cd/edrc/landgis/documents/DocumentService.java
grep -q "MALWARE_SCAN_PENDING" apps/api/src/main/java/cd/edrc/landgis/governance/DataGovernanceService.java
grep -q "MALWARE_SCAN_FAILED" apps/api/src/main/java/cd/edrc/landgis/governance/DataGovernanceService.java
grep -q "DIGITAL_SIGNATURE_INVALID" apps/api/src/main/java/cd/edrc/landgis/governance/DataGovernanceService.java
grep -q "DOCUMENT_VERSION_MISSING" apps/api/src/main/java/cd/edrc/landgis/governance/DataGovernanceService.java
grep -q "DOCUMENT_VERSION_QUARANTINED" apps/api/src/main/java/cd/edrc/landgis/governance/DataGovernanceService.java
grep -q "Document evidence intake" docs/architecture/phase-18-plan.md
grep -q "Document governance review hardening" docs/architecture/phase-19-plan.md
grep -q "Document scan and signature lifecycle review" docs/architecture/phase-20-plan.md
grep -q "Document quarantine and release controls" docs/architecture/phase-21-plan.md
grep -q "blocksAutomatedDispositionWhenDocumentHasActiveLegalHold" apps/api/src/test/java/cd/edrc/landgis/governance/DataGovernanceServiceTest.java
grep -q "blocksSensitiveExportWithoutRedactionAndApproval" apps/api/src/test/java/cd/edrc/landgis/governance/DataGovernanceServiceTest.java
grep -q "blocksExportWhenDocumentHasNoImmutableVersion" apps/api/src/test/java/cd/edrc/landgis/governance/DataGovernanceServiceTest.java
grep -q "blocksExportWhenLatestDocumentVersionHasPendingMalwareScan" apps/api/src/test/java/cd/edrc/landgis/governance/DataGovernanceServiceTest.java
grep -q "updatesDocumentVersionSafetyStatusWithAuditTrail" apps/api/src/test/java/cd/edrc/landgis/documents/DocumentServiceTest.java
grep -q "quarantinesDocumentVersionWithAuditTrail" apps/api/src/test/java/cd/edrc/landgis/documents/DocumentServiceTest.java
grep -q "blocksReleaseWhenDocumentVersionStillUnsafe" apps/api/src/test/java/cd/edrc/landgis/documents/DocumentServiceTest.java
grep -q "releasesQuarantinedDocumentVersionAfterCleanSafetyStatuses" apps/api/src/test/java/cd/edrc/landgis/documents/DocumentServiceTest.java
grep -q "blocksExportWhenLatestDocumentVersionIsQuarantined" apps/api/src/test/java/cd/edrc/landgis/governance/DataGovernanceServiceTest.java
grep -q "allowsStaffDocumentVersionSafetyStatusUpdate" apps/api/src/test/java/cd/edrc/landgis/documents/DocumentControllerSecurityTest.java
grep -q "allowsStaffDocumentVersionQuarantine" apps/api/src/test/java/cd/edrc/landgis/documents/DocumentControllerSecurityTest.java
grep -q "allowsStaffDocumentVersionRelease" apps/api/src/test/java/cd/edrc/landgis/documents/DocumentControllerSecurityTest.java
grep -q "opensSecurityWorkflowWhenSensitiveExportHasRedactionPlan" apps/api/src/test/java/cd/edrc/landgis/governance/DocumentExportRequestServiceTest.java
grep -q "blocksExportBeforeReviewWhenDocumentVersionIsUnsafe" apps/api/src/test/java/cd/edrc/landgis/governance/DocumentExportRequestServiceTest.java
grep -q "generatesPackageOnlyForApprovedExportRequestAndReturnsTokenOnce" apps/api/src/test/java/cd/edrc/landgis/governance/DocumentExportPackageServiceTest.java
grep -q "downloadsPackageWithUnexpiredTokenAndVerifiesChecksum" apps/api/src/test/java/cd/edrc/landgis/governance/DocumentExportPackageServiceTest.java
