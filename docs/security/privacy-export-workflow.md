# Privacy export workflow baseline

Protected document exports are high-risk operations. They can expose personal, legal, financial or security information, so they must be separated from ordinary document metadata reads and downloads.

## Baseline rule

An export request may be approved only when:

- the governed document exists;
- no active legal hold blocks export;
- required redaction is planned;
- a `DOCUMENT_EXPORT_REVIEW` workflow task exists;
- the workflow task is approved by a `SECURITY_OFFICER`;
- the final governance policy evaluation allows export.

## Blocked requests

Requests are blocked before workflow review when policy detects non-reviewable blockers such as:

- active legal hold;
- missing redaction plan for sensitive information.

Blocked requests remain auditable records. They do not produce export files.

## Pending requests

Requests that require approval but have a redaction plan open a workflow task assigned to `SECURITY_OFFICER`. Maker-checker controls in the workflow module continue to apply.

## Approved requests

Approval records permission to proceed with a future export-generation step. This phase does not generate export files and does not expose protected content through the governance API.
