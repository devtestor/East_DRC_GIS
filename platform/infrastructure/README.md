# Phase 9 Infrastructure Baseline

This directory contains deployment-readiness scaffolding for staging and controlled pilot environments.

It does not contain production credentials, cloud account identifiers, government endpoints or real integration secrets.

## Contents

- `env/staging.env.example`: required environment contract for a staging API instance.
- `compose/staging-compose.yml`: portable staging smoke-test topology.
- `kubernetes/api-deployment.yaml`: Kubernetes baseline for the API when the operating team has cluster capacity.
- `kubernetes/api-service.yaml`: internal ClusterIP service.
- `kubernetes/api-secrets.example.yaml`: example secret names and keys only.
- `terraform/`: provider-neutral variable contract and placeholder outputs.

## Promotion Rule

Staging or pilot deployment must not be treated as official registry operation. Production authorization, environment promotion, integration onboarding and migration rehearsal gates must be recorded in the pilot readiness workflow before any operational GO decision.
