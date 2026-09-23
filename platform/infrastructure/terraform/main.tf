locals {
  deployment_guardrails = {
    environment_name        = var.environment_name
    approved_region         = var.approved_region
    api_image_digest        = var.api_image_digest
    enable_development_seed = var.enable_development_seed
  }
}

check "development_seed_disabled" {
  assert {
    condition     = var.enable_development_seed == false
    error_message = "Development seed accounts must be disabled outside local development."
  }
}
