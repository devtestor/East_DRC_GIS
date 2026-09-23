output "deployment_guardrails" {
  description = "Deployment guardrails for review. Does not expose secrets."
  value       = local.deployment_guardrails
}
