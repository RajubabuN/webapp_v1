# Fixing Azure Container Registry Authentication Issue

> ⚠️ **Disclaimer**: Between WSL, Docker, Azure CLI, and Windows settings, there are more moving parts here than in a Swiss watch. If something suddenly breaks — it’s probably not your fault. Azure updates, WSL quirks, or Docker daemon mood swings might be the cause. Stay calm, read the error, Google wisely, and if all else fails — try turning it off and on again. Or ask ChatGPT — it doesn’t judge, only explains 😎.

<hr>

## The problem

When trying to authenticate with Azure Container Registry (ACR) using the command:

```bash
az acr login --name <acr-name>
```  

the process fails with a `DOCKER_COMMAND_ERROR`. This happens because the default authentication method requires Docker to be properly configured with the Azure CLI in the WSL environment.

## Root causes

1. Integration issues between Azure CLI and Docker in WSL.
2. Possible permission issues with the Docker socket.
3. Configuration mismatches between Windows Docker and WSL.

## Solution: direct Docker login

Use Docker login directly with credentials:

```bash
docker login <acr-name>.azurecr.io \
  -u <acr-name> \
  -p <password>
```  

## Where to get the password

The password required for authentication is the admin access key for your container registry. You can retrieve it from the Azure Portal:

1. Open the **Azure Portal**.
2. Navigate to your **Container Registry** resource.
3. In the left sidebar, click **Access keys** under **Settings**.
4. Enable **Admin user** if not already enabled.
5. Copy the **username** and either **password** to use in your Docker login command.
