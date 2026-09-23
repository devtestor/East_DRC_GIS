# Privacy and Data Governance

## Data Classes

- Public parcel information.
- Staff operational information.
- Protected personal information.
- Legal evidence.
- Financial information.
- Security information.
- Analytics data.

## Phase 1 Rules

- Public portals must use public projections, not protected registry tables.
- SMS and email must not include sensitive owner details.
- Documents are represented by metadata and object-storage references, not large database blobs.
- Document metadata includes classification, retention category, access policy, legal-hold flag, checksum, malware-scan status, and digital-signature status so access and retention controls can be enforced without exposing binary contents.
- Protected document metadata read access is filtered by server-side policy. The current slice permits the creator, an active member of the configured custodian organization and role, or a workflow-linked staff participant for workflow-scoped documents; list responses omit documents the actor cannot read.
- Appending a new protected document version currently requires the original document creator or an active member of the configured custodian organization and role. Custody fields are optional so older records remain valid, but production use still needs an approved custody-transfer workflow.
- Audit events should include enough traceability for accountability without logging secrets or full identity documents.
- Current document audit events record action, target type, target identifier, correlation ID, actor user ID, optional custodian organization ID, direct request remote IP address when parseable, bounded user-agent string, active registered `X-Device-Id` when supplied, source service, classification, and a small evidence summary containing document type, owner entity type, access policy, classification, custody-scope presence, and operation. They deliberately do not log document titles, filenames, object-storage keys, checksums, identity-document contents, owner personal details, free-form device names, or hardware identifiers. `X-Forwarded-For` is used only when the direct remote address matches `AUDIT_TRUSTED_PROXY_CIDRS`; otherwise forwarded headers are ignored. `X-Device-Id` is recorded only when it passes the safe format rule and matches an active, non-expired, non-revoked row in `identity.registered_devices`.
- Nonproduction seed data must be fictional.
- Public parcel search must use the `parcels.public_parcel_summaries` data-minimized projection and must not expose landholder identity, ownership evidence, protected documents, financial records, or dispute evidence.
- Party and parcel-interest APIs are protected staff-only endpoints. Public projections must not include party display names, ownership/use claims, shares, source notes, verification status, or supporting evidence links.
- Dispute-case document links remain staff-only and invoke document access authorization before metadata is returned; linking a document never makes it public.

## Required Field Documentation

Before adding sensitive fields, document purpose, authority, owner, classification, permitted users, sharing rules, retention, correction process, archival/deletion rule, and audit requirement.
