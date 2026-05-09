# Stage 1 — build the function package
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build
COPY pom.xml .
# Download dependencies first (layer caching)
RUN mvn dependency:go-offline -q
COPY src ./src
COPY host.json .
RUN mvn clean package -DskipTests -q

# Stage 2 — Azure Functions runtime image
FROM mcr.microsoft.com/azure-functions/java:4-java21

# Required env vars for Azure Functions
ENV AzureWebJobsScriptRoot=/home/site/wwwroot \
    AzureFunctionsJobHost__Logging__Console__IsEnabled=true

WORKDIR /home/site/wwwroot

# Copy function package built in stage 1
COPY --from=builder /build/target/azure-functions/petstore-orderreserver/ .

# Expose the default Azure Functions port
EXPOSE 80

