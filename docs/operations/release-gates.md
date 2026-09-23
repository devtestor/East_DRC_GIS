# Release and Pilot Deployment Gates

The platform is not ready for a pilot or production deployment until all gates below are recorded against a release artifact.

## Automated gates

- API tests and migration startup checks pass.
- Frontend type checks and production builds pass.
- Dependency audit has no unaccepted moderate-or-higher findings.
- Safe-default and credential-pattern checks pass.
- CodeQL and Trivy scans have no unaccepted findings.
- The container image is built from the repository revision and scanned before publication.
- SBOM and build-provenance attestations are attached to the release artifact.
- The image is published only through `tools/operations/publish-api-image.sh` after registry login and approval.
- Tagged releases publish the API image to GitHub Container Registry through `.github/workflows/release.yml`; the `pilot` environment must have required reviewers configured.

## Human approval gates

- Security owner approves the threat model, secrets configuration, TLS/proxy settings, and privileged-access controls.
- Data owner approves privacy classification, retention, public projections, and migration exceptions.
- Operations owner approves backups, restore rehearsal, monitoring, alert routing, and rollback.
- Legal/institutional owner confirms the operating mode and any external integration authorization.
- Pilot owner approves geography, users, training, support, stop conditions, and rollback.

## Hard stops

Do not proceed when development seeding is enabled, production credentials are in source control, a migration has not been rehearsed, a restore test has failed, a provider callback is unsigned/unverified, or an external service lacks formal authorization. Payment confirmation must never be treated as approval of a legal registry change.
