#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"

test -s "$root_dir/docs/architecture/phase-13-plan.md"
test -s "$root_dir/docs/security/privileged-operations.md"
test -s "$root_dir/docs/security/authorization-matrix.md"
test -s "$root_dir/apps/api/src/main/java/cd/edrc/landgis/workflow/HighRiskWorkflowPolicy.java"
test -s "$root_dir/apps/api/src/test/java/cd/edrc/landgis/workflow/HighRiskWorkflowPolicyTest.java"

grep -q 'HighRiskWorkflowPolicy' "$root_dir/apps/api/src/main/java/cd/edrc/landgis/workflow/WorkflowTaskService.java"
grep -q 'requireAllowedOpening' "$root_dir/apps/api/src/main/java/cd/edrc/landgis/workflow/WorkflowTaskService.java"
grep -q 'requireApprovalAllowed' "$root_dir/apps/api/src/main/java/cd/edrc/landgis/workflow/WorkflowTaskService.java"
grep -q 'PARCEL_GEOMETRY_APPROVAL' "$root_dir/apps/api/src/main/java/cd/edrc/landgis/workflow/HighRiskWorkflowPolicy.java"
grep -q 'OWNERSHIP_INTEREST_REVIEW' "$root_dir/apps/api/src/main/java/cd/edrc/landgis/workflow/HighRiskWorkflowPolicy.java"
grep -q 'REGISTERED_DEVICE_LIFECYCLE' "$root_dir/apps/api/src/main/java/cd/edrc/landgis/workflow/HighRiskWorkflowPolicy.java"
grep -q 'PILOT_GO_NO_GO' "$root_dir/apps/api/src/main/java/cd/edrc/landgis/workflow/HighRiskWorkflowPolicy.java"
grep -q 'Platform administrators remain technical operators' "$root_dir/docs/architecture/phase-13-plan.md"
grep -q 'must never be granted merely because a user is a system administrator' "$root_dir/docs/security/privileged-operations.md"
grep -q 'Privileged workflow policy' "$root_dir/docs/security/authorization-matrix.md"

echo "Privileged access checks passed"

