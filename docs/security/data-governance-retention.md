# Data governance, legal hold and retention baseline

This platform separates public parcel projections from protected registry, evidence, financial and security information. Phase 14 adds the first enforceable governance layer for records that must not be exposed, overwritten or destroyed without policy checks.

## Current scope

The first governed target is `documents.documents`.

Each document already carries:

- classification;
- retention category;
- access policy;
- legal-hold flag;
- custodian organization and role scope;
- immutable versions with checksums.

Phase 14 adds:

- retention-policy records;
- active/released legal-hold records;
- retention disposition decisions;
- privacy-export decisions.

## Legal hold rule

An active legal hold blocks automated disposition and privacy export. This is enforced by both:

- document-level hold state; and
- governance legal-hold records.

Only one active governance hold may exist for the same governed target.

## Privacy export rule

Exports of protected personal, legal evidence, financial or security information require:

- explicit approval;
- redaction planning;
- no active legal hold.

Public projection data must be exported from public projections, not from registry/evidence tables.

## Retention rule

Automated disposition is denied when:

- there is an active legal hold;
- the retention category has no policy;
- the minimum retention period has not elapsed;
- the policy marks the category as protected from automated disposal;
- approval is required and has not been completed by a future workflow.

No destructive purge job is introduced in this phase.
