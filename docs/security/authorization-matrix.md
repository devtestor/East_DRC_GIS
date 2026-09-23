# Initial Authorization Matrix

| Capability | Public | Citizen | Professional | Officer | Platform Admin |
| --- | --- | --- | --- | --- | --- |
| View legal-boundary disclaimer | Yes | Yes | Yes | Yes | Yes |
| Register account | Yes | Yes | Yes | Staff approval required | No special legal authority |
| View public parcel projection | Yes | Yes | Yes | Yes | Yes |
| View protected owner data | No | Own authorized cases only | Scoped cases only | Assigned jurisdiction/case only | No by default |
| Request legal change | No | Own/delegated cases | Scoped professional cases | Scoped official workflow | No by default |
| Create draft parcel geometry | No | No | Future assigned cases only | Current slice: authenticated staff only; not approved/current geometry | No by default |
| Approve parcel geometry | No | No | No | Evidence-linked cadastral workflow task only | No by default |
| View protected parties/interests | No | Own authorized cases only | Scoped cases only | Current slice: authenticated staff only | No by default |
| Verify ownership/right | No | No | No | Evidence-linked `LAND_TITLE_OFFICER` workflow task only; operational verification, not official title | No by default |
| Apply parcel restriction/caution | No | No | No | Current slice: authenticated staff only; future slice requires evidence-linked authority workflow | No by default |
| Request restriction release | No | No | No | Authenticated staff may request; no state mutation; task is scoped to the parcel | No by default |
| Approve restriction release | No | No | No | Scoped `LAND_TITLE_OFFICER`, evidence required, maker-checker enforced | No by default |
| Schedule dispute hearing | No | No | No | Authenticated staff; only `UNDER_REVIEW`/`REOPENED` cases; auditable transition | No by default |
| Record dispute decision | No | No | No | Evidence-backed `LAND_TITLE_OFFICER` workflow with maker-checker | No by default |
| Reopen dispute case | No | No | No | Evidence-backed `LAND_TITLE_OFFICER` workflow from a terminal case state | No by default |
| File dispute appeal | No | No | No | Authenticated staff; case must have a decision/resolution/closure state | No by default |
| Approve legal change | No | No | No | Explicit maker-checker authority | No by default |
| View protected document metadata | No | Own authorized cases only | Scoped cases only | Creator, active custodian-role member, or workflow-linked staff participant only in current slice | No by default |
| Append protected document version | No | No | No | Creator or active custodian-role member only in current slice | No by default |
| Manage registered devices | No | No | No | Current slice: authenticated staff may enroll/list; suspend/revoke/expire require security-officer maker-checker workflow | No by default |
| Configure UPI rules | No | No | No | No | Privileged workflow only |
| Export audit records | No | No | No | Compliance scope only | Privileged workflow only |

Platform administration is technical administration and does not imply authority to change legal land records.

Phase 2 local development uses a fictional seeded staff account. It grants `ROLE_STAFF` only for exercising protected staff endpoints locally and creates a fictional active `CADASTRAL_OFFICER` organization membership scoped to the fictional `NK-FICTIONAL` province/jurisdiction so workflow claim checks can run in development. This is not a production authorization model.

The current workflow slice records the user ID that requested an approval-required parcel status transition, requires a staff user with an active membership matching the task role and parcel administrative jurisdiction to claim the task, allows only the claiming user to decide it, and blocks the requester user from approving that same task. This is an initial maker-checker, role-scope, and jurisdiction-scope safeguard only; production authorization still needs parcel sensitivity checks, task assignment policy, and risk-based approval thresholds.

Protected document metadata is no longer exposed to every authenticated staff user. A document creator can read the metadata they created, active members of the configured custodian organization and role can read and append immutable versions, and documents with the `workflow-task-and-authorized-staff` access policy can be read by staff users linked to a workflow task that references the document as controlled evidence. This is still an initial custody model; production authorization must add custody-transfer approvals, organization-type policy, document sensitivity, and retention/legal-hold checks.

Registered-device enrollment and listing are staff-only in the current slice so local and pilot workflows can establish traceable devices. High-risk suspend, revoke, and expire requests now open `REGISTERED_DEVICE_LIFECYCLE` workflow tasks assigned to `SECURITY_OFFICER`; the shared workflow controls require task claim, evidence before approval, and maker-checker separation before the device row is mutated. Production use must still add finer device-administrator roles, assignment policy, organization/jurisdiction scope, and remote mobile-device revocation controls.

Party and interest records are protected staff-only operational data in the current slice. Ownership-interest review requires a workflow task, task claim, linked evidence before approval, maker-checker separation, and an active `LAND_TITLE_OFFICER` membership. A `VERIFIED` interest is not a public ownership certificate and must not be interpreted as official title without future authorized legal workflows.

Parcel restrictions/cautions are protected legal-evidence controls. The current slice allows staff to apply active restrictions and enforces blocking of ownership-interest mutations in both service code and a database trigger. Production release/removal of restrictions must require evidence-linked maker-checker approval and the appropriate court or administrative authority.
