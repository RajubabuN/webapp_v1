# Pet Store Application for CloudX Java Azure Dev Course

The project is the Pet Store Application for the Azure Cloud Introductory course for Java Developers (CloudX Java Azure Dev).

The Pet Store application is a web-based Java application that leverages the Spring Boot framework to build RESTful APIs, manage dependencies, and handle application configuration. It provides a straightforward online pet store experience, allowing users to perform essential operations such as browsing pet products, adding items to their shopping cart, placing orders, and completing transactions. This application serves as an ideal foundation for your Azure learning journey.

The PetStoreApp consists of four microservices that work together to create a cohesive and robust application. These microservices are:

1. PetStoreApp (`./petstore/petstoreapp`): The front-facing Java Spring Boot web application that serves as the user interface for the Pet Store system.
2. PetService (`./petstore/petstorepetservice`): A Java Spring Boot microservice that operates on the back end and delivers Pet data to consumers.
3. ProductService (`./petstore/petstoreproductservice`): Another Java Spring Boot microservice on the back end, responsible for delivering Pet Product data to consumers.
4. OrderService (`./petstore/petstoreorderservice`): A back-end Java Spring Boot microservice dedicated to managing Pet Store orders.

## Running the PetStore Application with Docker Compose

### Prerequisites

Before proceeding, ensure you have the Docker installed on your system.

### Step 1: Clone the Repository

Open a terminal or command prompt, and clone the PetStore application repository to your local machine:

```bash
git clone https://git.epam.com/epm-cdp/cloudx-assosiate-ms-azure-developer/cloudx-java-azure-dev.git
cd cloudx-java-azure-dev/petstore
```

### Step 2: Verify docker-compose.yml

Ensure that the `docker-compose.yml` file is present in the root directory of the PetStore application project:

```bash
ls
```

### Step 3: Running the Application with Docker Compose

From the root directory of your PetStore application project (where the `docker-compose.yml` file is located), run the following command to build and start the containers:

```bash
docker-compose up -d --build
```

The `-d` flag tells Docker Compose to run the services in the background, and the `--build` flag ensures that Docker Compose builds any updated images from the Dockerfiles.

### Step 4: Access the PetStore Application

After the services are up and running, you can access the PetStore application and Swagger pages:

- PetStoreApp: Open your web browser and go to [http://localhost:8080/](http://localhost:8080/).
- Swagger Pages:
    - PetService: [http://localhost:8081/swagger-ui/](http://localhost:8081/swagger-ui/)
    - ProductService: [http://localhost:8082/swagger-ui/](http://localhost:8082/swagger-ui/)
    - OrderService: [http://localhost:8083/swagger-ui/](http://localhost:8083/swagger-ui/)

### Step 5: Stopping the Containers
To stop and remove the containers, use the following command:

```bash
docker-compose down
```

This will stop the running containers and remove the associated resources.

## Running the PetStore Application with IntelliJ IDEA

To run the microservices as a complete application, ensure that each microservice is configured to run on a different port, as shown below:

- PetStoreApp: 8080
- PetService: 8081
- ProductService: 8082
- OrderService: 8083

In IntelliJ IDEA, you can find the corresponding running configurations for each application.

After the services are up and running, you can access the PetStore application and Swagger pages:

- PetStoreApp: Open your web browser and go to [http://localhost:8080/](http://localhost:8080/).
- Swagger Pages:
    - PetService: [http://localhost:8081/swagger-ui/](http://localhost:8081/swagger-ui/)
    - ProductService: [http://localhost:8082/swagger-ui/](http://localhost:8082/swagger-ui/)
    - OrderService: [http://localhost:8083/swagger-ui/](http://localhost:8083/swagger-ui/)

