
terraform {
    required_providers {
        azurerm = {
            source  = "hashicorp/azurerm"
            version = ">=3.7.0"
        }
        google = {
            source  = "hashicorp/google"
            version = ">=5.10.0"
        }
        aws = {
            source  = "hashicorp/aws"
            version = ">=5.31.0"
        }
    }
 }

provider "azurerm" {
  features {}
}

provider "google" {
    project = var.gcp_project
    region = var.gcp_region
}

