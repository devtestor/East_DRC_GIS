# Privileged Operations Baseline

Privileged operations are actions that can affect legal, cadastral, security, pilot-readiness or audit-sensitive state. They must never be granted merely because a user is a system administrator.

## Current Controls

- High-risk workflow actions are registered in `HighRiskWorkflowPolicy`.
- Task creation validates workflow type, target type, requested action and checker role.
- Approval validates the same policy again before mutation-specific services apply changes.
- Approval requires:
  - a claimed workflow task;
  - the deciding actor must be the claimant;
  - the deciding actor must not be the requester;
  - at least one linked evidence record;
  - a scoped role membership enforced by `WorkflowRoleScopeAuthorizer`.

## Current Privileged Catalog

| Workflow | Purpose | Required role |
| --- | --- | --- |
| `PARCEL_STATUS_TRANSITION` | Controlled parcel lifecycle changes | `CADASTRAL_OFFICER` |
| `PARCEL_GEOMETRY_APPROVAL` | Publish current approved geometry | `CADASTRAL_OFFICER` |
| `OWNERSHIP_INTEREST_REVIEW` | Verify operational interest records | `LAND_TITLE_OFFICER` |
| `PARCEL_RESTRICTION_RELEASE` | Release a parcel restriction | `LAND_TITLE_OFFICER` |
| `DISPUTE_CASE_REVIEW` | Start protected dispute review | `LAND_TITLE_OFFICER` |
| `DISPUTE_CASE_DECISION` | Record operational dispute decision | `LAND_TITLE_OFFICER` |
| `DISPUTE_CASE_REOPEN` | Reopen terminal dispute case | `LAND_TITLE_OFFICER` |
| `REGISTERED_DEVICE_LIFECYCLE` | Suspend, revoke or expire enrolled devices | `SECURITY_OFFICER` |
| `PILOT_SIGNOFF_REVIEW` | Review pilot readiness signoffs | configured owner role |
| `PILOT_GO_NO_GO` | Approve pilot go/no-go | `PROVINCIAL_LAND_ADMINISTRATOR` |

## Explicit Limits

- These controls do not make the platform an official registry.
- Approval of operational records does not create legally valid land title.
- Public users cannot see protected owner data through privileged workflows.
- Production use still requires owner-approved IAM, emergency access, session monitoring and institutional authorization.

## Change Rule

When adding a new high-risk workflow:

1. Add a policy rule.
2. Add a unit test for the rule.
3. Add an authorization-matrix entry.
4. Confirm evidence and maker-checker controls are still required.
5. Confirm the approving role is not a platform administrator by default.

