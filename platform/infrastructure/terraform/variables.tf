variable "environment_name" {
  description = "Deployment environment name, for example staging or pilot."
  type        = string
}

variable "approved_region" {
  description = "Approved hosting region. Must be selected by the platform owner."
  type        = string
}

variable "api_image_digest" {
  description = "Approved immutable API image digest from the release workflow."
  type        = string
}

variable "enable_development_seed" {
  description = "Must remain false outside local development."
  type        = bool
  default     = false
}
