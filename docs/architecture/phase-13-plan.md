# Phase 13 Access-Control Hardening and Privileged Operations

## Outcome

Make high-risk workflow operations explicit in code, tests and documentation so legal, cadastral, security and pilot-readiness actions cannot be approved unless they are registered in the privileged workflow policy.

## Scope Boundary

Phase 13 does not implement a full external policy engine, final production IAM, emergency break-glass tooling or live privileged-access management provider. It hardens the current modular-monolith workflow slice by centralizing high-risk action registration and adding repository-native guardrails.

## Affected Modules

- Workflow and maker-checker task services.
- Parcel, rights, disputes, registered-device and pilot-readiness workflows that open approval tasks.
- Security documentation and CI guardrails.

## Access-Control Changes

- High-risk workflow task creation now passes through `HighRiskWorkflowPolicy`.
- Approval of workflow tasks is denied when the workflow/action/target combination is not registered in the policy.
- Policy rules define required checker roles for known high-risk actions.
- Existing maker-checker controls still require task claim, evidence before approval and requester/checker separation.
- Platform administrators remain technical operators and are not granted legal-record approval authority by default.

## High-Risk Actions Covered

- Parcel status transitions.
- Parcel geometry approval/publication.
- Ownership-interest verification.
- Restriction release.
- Dispute review, decision and reopening.
- Registered-device suspend/revoke/expire workflows.
- Pilot sign-off and pilot go/no-go decisions.

## Security Considerations

- New privileged workflow actions must be added to the policy and tested before they can be approved.
- Role assignment alone is insufficient; task claim scope and maker-checker separation still apply.
- Rejections remain allowed for claimed tasks so unsafe or unsupported requests can be closed without granting mutation authority.
- Production use still requires a stronger ABAC policy engine, emergency-access procedure, and privileged-session recording.

## Privacy Considerations

- Privileged policy does not expand read access to protected owner, document, dispute or financial data.
- Approval reasons and evidence summaries must not include secrets, full identity documents or raw payment payloads.

## Failure Modes

- A new high-risk workflow is added without a policy rule and becomes unapprovable.
- A workflow is assigned to the wrong role and is blocked at creation or approval.
- A legitimate pilot-specific signoff uses a non-`APPROVE_` action and is blocked.
- An operator attempts to approve their own request and remains blocked by maker-checker controls.

## Acceptance Criteria

- High-risk workflow policy exists in backend code.
- Workflow task opening and approval both consult the policy.
- Tests prove unregistered privileged workflows cannot be approved.
- Authorization documentation lists privileged operation behavior and limits.
- CI validates that the policy, tests and documentation remain present.

