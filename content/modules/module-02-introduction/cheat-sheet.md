# Module 2: Azure CLI. Introduction to Azure Container Registry and App Services

## Azure CLI Commands Cheat Sheet for Module 2

### Resource Groups

1. **Create a resource group**
    ```bash
    az group create --name <resource_group_name> --location <location>
    ```
   - `--name`: Name of the resource group.
   - `--location`: Azure location where the resource group will be created.

2. **List all resource groups in JSON format**
    ```bash
    az group list
    ```

3. **List all resource groups in table format**
    ```bash
    az group list --output table
    ```
   - `--output table`: Output format as a table.

4. **Show details of a specific resource group**
    ```bash
    az group show --name <resource_group_name>
    ```
   - `--name`: Name of the resource group to show details for.

5. **List resource groups filtered by location in table format**
    ```bash
    az group list --query "[?location=='<location>'].{Name:name, Location:location}" --output table
    ```
   - `--query`: Query string to filter the resource groups.
   - `--output table`: Output format as a table.

6. **Save resource group ID to variable**
    ```bash
    RG_ID=$(az group show --name <resource_group_name> --query 'id' -o tsv)
    ```
   - `--name`: Name of the resource group.
   - `--query 'id'`: Query to select the ID of the resource group.
   - `-o tsv`: Output format as tab-separated values.

7. **Delete a resource group**
    ```bash
    az group delete --name <resource_group_name> --yes --no-wait
    ```
   - `--name`: Name of the resource group to delete.
   - `--yes`: Automatic confirmation of deletion.
   - `--no-wait`: Do not wait for the operation to complete.

### Azure Container Registry (ACR)

1. **Create Azure Container Registry**
    ```bash
    az acr create \
        --resource-group <resource_group_name> \
        --name <acr_name> \
        --sku Basic \
        --admin-enabled true
    ```
   - `--resource-group`: Name of the resource group.
   - `--name`: Name of the Azure Container Registry.
   - `--sku Basic`: Specifies the Basic tier.
   - `--admin-enabled true`: Enable admin user.

2. **Show ACR credentials in YAML format**
    ```bash
    az acr credential show --name <acr_name> --output yaml
    ```
   - `--name`: Name of the Azure Container Registry.
   - `--output yaml`: Output format as YAML.

3. **List repositories in ACR in table format**
    ```bash
    az acr repository list --name <acr_name> --output table
    ```
   - `--name`: Name of the Azure Container Registry.
   - `--output table`: Output format as a table.

4. **Show tags in a repository in table format**
    ```bash
    az acr repository show-tags --name <acr_name> --repository <repository-name> --output table
    ```
   - `--name`: Name of the Azure Container Registry.
   - `--repository`: Name of the repository.
   - `--output table`: Output format as a table.

5. **Put images in descendant order in table format**
    ```bash
    az acr repository show-tags --name <acr_name> --repository <repository-name> --orderby time_desc --output table
    ```
   - `--name`: Name of the Azure Container Registry.
   - `--repository`: Name of the repository.
   - `--orderby time_desc`: Orders the tags by time in descending order to find the oldest image.
   - `--output table`: Output format as a table.
 
6. **Delete an image by tag**
    ```bash
    az acr repository delete --name <acr_name> --image <repository-name>:<tag> --yes
    ```
   - `--name`: Name of the Azure Container Registry.
   - `--image`: Name of the image and tag to delete.
   - `--yes`: Automatic confirmation of deletion.

### Azure App Services

1. **Create App Service Plan**
    ```bash
    az appservice plan create \
        --name <appservice_plan_name> \
        --resource-group <resource_group_name> \
        --location <location> \
        --sku B1 \
        --is-linux
    ```
   - `--name`: Name of the App Service Plan.
   - `--resource-group`: Name of the resource group.
   - `--location`: Azure location where the App Service Plan will be created.
   - `--sku B1`: Specifies the Basic B1 tier.
   - `--is-linux`: Indicates Linux-based environment.

2. **Create and deploy web app**
    ```bash
    az webapp create \
        --resource-group <resource_group_name> \
        --plan <appservice_plan_name> \
        --name <web_app_name> \
        --deployment-container-image-name <acr_name>.azurecr.io/<repository-name>:<tag>
    ```
   - `--resource-group`: Name of the resource group.
   - `--plan`: Name of the App Service Plan.
   - `--name`: Name of the web app.
   - `--deployment-container-image-name`: Fully qualified path to the Docker image.
