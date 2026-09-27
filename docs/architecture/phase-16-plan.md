# Phase 16: secure export package generation and delivery

## Outcome

Phase 16 turns an approved governed document export request into a controlled, checksum-verifiable export package. It still does not create legally valid land titles or unrestricted bulk extracts.

## Affected modules

- `governance`: export package lifecycle, token-bound delivery and audit records.
- `documents`: source document content and version metadata.
- `audit`: package generation and download security events.
- `security`: authenticated staff governance route continues to protect export package APIs.

## Database changes

- New `governance.export_packages` table.
- Each approved export request can have only one package.
- Packages record:
  - source document and document version;
  - sandbox object-storage key;
  - manifest checksum;
  - package checksum;
  - package size;
  - hashed delivery token;
  - expiry time;
  - generator identity;
  - download count and last-download metadata.

## API changes

- `POST /api/v1/governance/export-requests/{requestId}/packages`
- `GET /api/v1/governance/export-packages/{packageId}`
- `GET /api/v1/governance/export-packages/{packageId}/download?token=...`

Package creation returns the delivery token once. Later metadata reads never return the token.

## Security and privacy considerations

- Package generation requires an already approved export request.
- Delivery requires staff authentication and the unexpired delivery token.
- The stored token is a SHA-256 hash, not the bearer secret.
- Downloads verify the generated package checksum before returning content.
- Package generation and download are recorded as security audit events.
- The sandbox package contains a notice that it is not an official title or certificate.
- Unsecured SMS/email notification of sensitive content remains prohibited.

## Failure modes

- Export request missing: package generation fails.
- Export request not approved: package generation fails.
- Package already exists: duplicate generation fails.
- Source document version missing: package generation/download fails.
- Token missing/invalid/expired: download fails.
- Package checksum mismatch: download fails safely.

## Acceptance criteria

- Approved export requests can produce one governed package.
- Pending, rejected or blocked requests cannot produce packages.
- Package responses expose delivery tokens only at creation time.
- Package records include manifest and package checksums.
- Download requires the correct unexpired token.
- Download records audit metadata and increments download count.
- Tests and CI guardrails pass.
