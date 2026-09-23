# OpenAPI Contracts

Generated OpenAPI output will be published here as the API stabilizes. Phase 1 exposes the runtime OpenAPI endpoint through Springdoc at `/v3/api-docs` when the API is running.

Current checked-in contract slices:

- `registered-device-workflow.yaml` documents the staff-only registered-device enrollment and lifecycle maker-checker workflow endpoints, including task claiming, evidence links, and device lifecycle decisions.
- The parcel-restriction release workflow is exposed through the runtime OpenAPI document and uses:
  - `POST /api/v1/parcels/{parcelId}/restrictions/{restrictionId}/release-requests` to open an evidence-backed release task without changing restriction state.
  - `POST /api/v1/parcels/{parcelId}/restrictions/tasks/{taskId}/decisions` to approve or reject the task; approval changes only the targeted active restriction to `RELEASED`.
