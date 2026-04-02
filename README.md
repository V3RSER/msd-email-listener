# Email Listener

This is a Spring Boot application that listens for emails and processes them.

## Prerequisites

- Java 21
- Maven
- Docker

## Getting Started

1.  **Start the database:**

    ```bash
    docker-compose up -d
    ```

2.  **Run the application:**

    ```bash
    mvn spring-boot:run
    ```

## Configuration

The application uses the following environment variables for configuration:

- `AZURE_CLIENT_ID`: The client ID of your Azure AD app registration.
- `AZURE_CLIENT_SECRET`: The client secret of your Azure AD app registration.
- `AZURE_TENANT_ID`: The tenant ID of your Azure AD app registration.
- `DB_HOST`: The host of the database.
- `DB_PORT`: The port of the database.
- `DB_NAME`: The name of the database.
- `DB_USERNAME`: The username for the database.
- `DB_PASSWORD`: The password for the database.
