# Module 8: Key Vault

## Task

The source code is available [here](../../../petstore).

Azure Key Vault is a service that offers centralized secrets management, granting comprehensive control over access policies and audit history. In this module, you will practice working with secrets from Azure Key Vault without necessitating any code alterations.

> **NOTE:** For the deployment of services in this and all subsequent tasks, you may choose to use either **Azure App Services or Azure Container Apps** (the diagrams will depict App Services).

**Please complete the following task:**

1. Create a Key Vault instance.
2. Store the Azure Database for PostgreSQL URL, username, and password (or connection string) as secrets in the Key Vault.
3. Enable managed identity for the two apps that use the Azure Database for PostgreSQL.
4. Create a Key Vault access policy for the two apps that use the Azure Database for PostgreSQL.
5. Obtain the unique secret URIs associated with the stored database secrets.
6. Set your applications' environment variables to reference the URIs, thus ensuring secure access to the database credentials.
7. Update the configuration of your applications to use the environment variables for the database URL, username, and password (or connection string).
8. (Optional) Utilize Azure CLI to automate resource deployment.

**Definition of Done:**

1. Database credentials are not stored in configuration files or code.
2. A Key Vault is created with secret values assigned for the database URL, username, and password (or connection string).
3. Managed identity is enabled for the Azure App Services or Azure Container Apps.
4. A Key Vault access policy is created with the appropriate permissions for the managed identity.
5. Environment variables have been configured to reference the Key Vault secrets via their unique URLs.
6. The applications successfully utilize the environment variables for the database URL, username, and password (or connection string) in their configuration settings.

<img src="images/scheme.png" width="550" style="margin: 20px 0; display: inline-block;"/>

<hr>

**Consider providing the following screenshots as evidence of your task execution:**

- Screenshots displaying the created Key Vault instance.
- Screenshots illustrating the database URL, username, and password (or connection string) stored as secrets in the Key Vault.
- Screenshots showing the enabled managed identity for the App Services or Container Apps.
- Screenshots highlighting the created Key Vault access policy for the managed identity.
- Screenshots revealing the unique secret URIs associated with the stored database URL, username, and password (or connection string).
- Screenshots displaying your apps' environment variables that reference the stored secret URIs for database URL, username, and password (or connection string).
- Screenshots showing configuration within your apps that utilizes environment variables to reference the stored secret URIs for the database URL, username, and password (or connection string).
- A screenshot showing the list of Azure resources that correspond to the diagram.

<hr>

<div style="border: 1px solid #ccc; background-color: #eee;">
  <ul>
    <li>When presenting the results of the practical tasks, please <a href="../common/presenting-results/presenting-results.md">follow these guidelines</a>.</li>
    <li><strong>When you have completed the task, attach the file(s) to the "Answer" field. Files should include a PDF/DOCX file with screenshots (optionally, you can also add a GIF/MP4 file). Please add a link to the updated Pet Store solution in a public Git repository to your PDF/DOCX file. Click "Submit."</strong></li>
    <li>Delete unnecessary resources.</li>
  </ul>
</div>
<hr>

>**IMPORTANT:** Leaving resources running can result in additional costs. Either delete resources individually or remove the entire set of resources by deleting the resource group.
