# Risk Register

| Risk | Impact | Phase 1 Treatment |
| --- | --- | --- |
| Legal authority misunderstood | Invalid public claims or unsafe workflows | Legal-boundary ADR and disclaimer surfaces |
| Insider misuse | Unauthorized registry change | Platform admin has no legal authority by default; maker-checker documented |
| Weak auditability | Loss of traceability | Append-only audit table and correlation IDs |
| Broken database migration | API cannot start or schema drifts | CI runs API startup against PostgreSQL/PostGIS and verifies Flyway migration validation |
| Hard-coded administrative hierarchy | Poor expansion across provinces | Administration kept as configurable module for Phase 2 |
| Dependency supply-chain risk | Build compromise | CI npm moderate-or-higher audit and SBOM artifact generation; future SAST, container scanning, provenance attestations, and artifact signing still required |
| Workflow engine choice too early | Poor fit or operational burden | Adapter-first approach; decision deferred before Phase 4 |
| Read-only invalid `.git` directory | No commits or normal status checks | Reported as environment blocker |
| Registry data leaked publicly | Privacy breach | Public projection principle documented |
