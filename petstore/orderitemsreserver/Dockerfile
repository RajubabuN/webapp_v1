# Stage 1: Build
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Azure Functions runtime container
FROM mcr.microsoft.com/azure-functions/java:4-java17-appservice

# Copy the function app artifacts from the build stage
ENV AzureWebJobsScriptRoot=/home/site/wwwroot \
    AzureFunctionsJobHost__Logging__Console__IsEnabled=true

COPY --from=builder /app/target/azure-functions/OrderItemsReserver22 /home/site/wwwroot
