
This is my personalized version of the Quarkus extensions documentation:
- [Azure Blob Storage](https://github.com/quarkiverse/quarkus-azure-services/tree/main/integration-tests)


## Configuring Azure Blob Storage

Quarkus Azure Blob Storage extension provides a container data services allowing you to test your application locally.
This data service is based on the [Azurite container](https://hub.docker.com/_/microsoft-azure-storage-azurite).

Nevertheless, you will need to use a real Azure Blob Storage account to deploy your application in production, 
let's see how to configure it.



### Logging into Azure

```bash
az login

RESOURCE_GROUP_NAME=<resource-group-name>

az group create \
    --name ${RESOURCE_GROUP_NAME} \
    --location westeurope
```

### Creating Azure Storage Account
    
```bash
STORAGE_ACCOUNT_NAME=<unique-storage-account-name>

az storage account create \
    --name ${STORAGE_ACCOUNT_NAME} \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --location westeurope \
    --sku Standard_LRS \
    --kind StorageV2 \
    --allow-blob-public-access false

export QUARKUS_AZURE_STORAGE_BLOB_CONNECTION_STRING=$(az storage account show-connection-string \
    --resource-group ${RESOURCE_GROUP_NAME} \
    --name ${STORAGE_ACCOUNT_NAME} \
    --query connectionString -o tsv)

echo "The value of 'quarkus.azure.storage.blob.connection-string' is: ${QUARKUS_AZURE_STORAGE_BLOB_CONNECTION_STRING}"

```

### Creating Azure App Configuration

```bash

export APP_CONFIG_NAME=<unique-app-config-name>

az appconfig create \
    --name "${APP_CONFIG_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    --location eastus
    
export QUARKUS_AZURE_APP_CONFIGURATION_ENDPOINT=$(az appconfig show \
  --resource-group "${RESOURCE_GROUP_NAME}" \
  --name "${APP_CONFIG_NAME}" \
  --query endpoint -o tsv)

credential=$(az appconfig credential list \
    --name "${APP_CONFIG_NAME}" \
    --resource-group "${RESOURCE_GROUP_NAME}" \
    | jq 'map(select(.readOnly == true)) | .[0]')

export QUARKUS_AZURE_APP_CONFIGURATION_ID=$(echo "${credential}" | jq -r '.id')

export QUARKUS_AZURE_APP_CONFIGURATION_SECRET=$(echo "${credential}" | jq -r '.value')


```

The values of environment variable QUARKUS_AZURE_APP_CONFIGURATION_ENDPOINT / QUARKUS_AZURE_APP_CONFIGURATION_ID / QUARKUS_AZURE_APP_CONFIGURATION_SECRET will be fed into config properties quarkus.azure.app.configuration.endpoint / quarkus.azure.app.configuration.id / quarkus.azure.app.configuration.secret of azure-app-configuration extension in order to set up the connection to the Azure App Configuration store.