
resource "google_storage_bucket" "gcp_quarkus_learning" {
  name     = var.gcp_bucket_name
  location = var.gcp_region

  force_destroy = true
}


