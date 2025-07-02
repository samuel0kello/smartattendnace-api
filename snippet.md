# Smart Attendance Backend Deployment

This document outlines the steps taken to fix **GitHub Actions**, create an **Azure App Service**, and deploy the backend using GitHub Actions to make the app live on Azure. These steps can be used for reference or replicated for similar projects.

---

## **Overview**
This project involves hosting the **Smart Attendance Backend**, built using the **Ktor framework**, on **Azure App Service** using **GitHub Actions** for CI/CD. Key elements include:
- Setting up **Azure App Service**.
- Building and deploying the backend using **GitHub Actions**.
- Deployment routing and Swagger integration for API documentation.

---

## **Steps to Host and Deploy Ktor Backend on Azure App Service**

---

### 1. **Develop Backend API Using Ktor**
Ensure your Ktor backend API is fully functional locally:
- Build and test the application locally.
- Use `Swagger` for API documentation (e.g., Swagger UI is defined in `configureSwagger.kt`):
  ```kotlin
  fun Application.configureSwagger() {
      routing {
          swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
      }
  }
  ```

#### Configuration for Port Compatibility on Azure:
Azure exposes the application via a system-defined environment variable `PORT`. Update your Ktor `main` entry point to dynamically use this `PORT`:
```kotlin
val serverPort = System.getenv("PORT")?.toInt() ?: 8080
embeddedServer(Netty, port = serverPort) {
    // Your application modules initialization
}.start(wait = true)
```

---

### 2. **Create Azure Resources**

#### **Step A: Create Azure App Service**
1. Log in to the [Azure Portal](https://portal.azure.com).
2. Navigate to **App Services** → **Create App Service**.
3. Provide the required details:
   - Subscription: Select your subscription.
   - Resource Group: Create a new group, e.g., `SmartAttendanceRG`.
   - App Service Name: `smartattendance-backend`.
   - Runtime Stack: Select the appropriate stack (e.g., Linux, Java 17).
   - Region: Select a region close to your users (e.g., `Central Canada`).

4. **Create and Review**: Once all fields are filled, click **Create** to deploy the App Service.

---

#### **Step B: (Optional) Azure Container Registry**
If you plan to deploy a Dockerized application:
1. Create a Container Registry (ACR):
   - Go to **Container Registry** → **Create**.
   - Enter ACR name: `smartattendanceapiregistry`.
   - Choose the same subscription and resource group as the App Service.
   - Enable Admin Access for authentication with `docker login`.

2. Build and tag a Docker image for your app:
   ```bash
   docker build -t smartattendanceapiregistry.azurecr.io/ktor-backend:latest .
   ```

3. Push the Docker image to ACR:
   ```bash
   docker push smartattendanceapiregistry.azurecr.io/ktor-backend:latest
   ```

---

### 3. **Setup GitHub Secrets**
To allow GitHub Actions to interact with Azure resources:
1. Go to your repository → **Settings → Secrets and Variables → Actions**.
2. Add the following secrets:
   - **`AZURE_CLIENT_ID`**: The Client ID from your Service Principal.
   - **`AZURE_CLIENT_SECRET`**: The Client Secret from your Service Principal.
   - **`AZURE_TENANT_ID`**: The Tenant ID from your Service Principal.
   - **`AZURE_SUBSCRIPTION_ID`**: Your Azure Subscription ID.
   - Optional (for Docker deployments):
     - **`AZURE_ACR_USERNAME`**: ACR username.
     - **`AZURE_ACR_PASSWORD`**: ACR password.

Details of the Service Principal can be generated via Azure CLI:
```bash
az login
az ad sp create-for-rbac --name "smartattendance-sp" --role contributor --scopes /subscriptions/<your-subscription-id>
```

---

### 4. **Configure GitHub Actions Workflow for Deployment**
The `.github/workflows/deploy.yml` file handles CI/CD for deployment. Below is the workflow script:

```yaml
name: Deploy Backend to Azure

on:
  push:
    branches:
      - main

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    steps:
      # Step 1: Checkout the code
      - name: Checkout Code
        uses: actions/checkout@v4

      # Step 2: Set up Java JDK for Ktor
      - name: Set up JDK 17 for Ktor
        uses: actions/setup-java@v3
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'gradle'

      # Step 3: Build the backend service
      - name: Build the Application
        run: |
          # Ensure Gradle is working correctly
          ./gradlew build

      # Step 4: Log in to Azure for Deployment
      - name: Log in to Azure
        uses: azure/login@v1
        with:
          creds: ${{ secrets.AZURE_CREDENTIALS }}

      # Step 5: Deploy to App Service
      - name: Deploy to Azure App Service
        uses: azure/webapps-deploy@v2
        with:
          app-name: "smartattendance-backend"
          slot-name: "production"
```

Save this as `.github/workflows/deploy.yml` in your repository.

---

### 5. **Deploy Application**
1. Push your changes to the `main` branch.
2. GitHub Actions will automatically trigger based on the `deploy.yml` workflow.
3. Monitor the deployment logs to verify the success of the build and deployment.

---

### 6. **Verify Application Live URL**
Once deployed, verify your application in the browser:

1. Open the deployed App Service link:  
   ```
   https://smartattendance-backend-bug4bxgybhbwecey.canadacentral-01.azurewebsites.net
   ```

2. Access the Swagger API UI for testing:
   ```
   https://smartattendance-backend-bug4bxgybhbwecey.canadacentral-01.azurewebsites.net/swagger
   ```

---

## **Troubleshooting**

### Common Issues and Fixes:

#### **Application Error**
- Check Azure App Service logs:
  Go to **Monitoring → Log Stream** in Azure Portal and analyze the startup logs.

#### **Port Binding Issue**
- Ensure your application reads the `PORT` variable:
  ```kotlin
  val serverPort = System.getenv("PORT")?.toInt() ?: 8080
  ```

#### **Environment Variables Missing**
- Ensure all required environment variables are configured in Azure:
  Go to **Configuration → Application Settings** in App Service and add the variables.

#### **Workflow Errors**
- Check GitHub Actions logs under **Actions** in your repository to debug CI/CD pipeline issues.

---

## **Conclusion**
By following this guide, you have:
1. Setup and configured Azure resources for hosting the Ktor backend.
2. Built and deployed the application using GitHub Actions.
3. Provided an interactive Swagger UI for documentation and API testing.