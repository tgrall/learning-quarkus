




resource "azurerm_resource_group" "az_quarkus_learning" {
  name     = var.azure_rg_name
  location = var.azure_region
}

resource "azurerm_storage_account" "az_quarkus_learning" {
  name                     = var.azure_account_name
  resource_group_name      = azurerm_resource_group.az_quarkus_learning.name
  location                 = var.azure_region
  account_kind             = "StorageV2"
  account_tier             = "Standard"
  account_replication_type = "LRS"
}

resource "azurerm_storage_container" "az_quarkus_learning" {
  name                  = var.azure_container_name
  storage_account_name  = azurerm_storage_account.az_quarkus_learning.name
  container_access_type = var.azure_access_type
}


output "connection_string" {
  value = azurerm_storage_account.az_quarkus_learning.primary_connection_string
  description = "The connection string of the created storage account. Use this to connect to the storage table from applications."
   sensitive = true
}