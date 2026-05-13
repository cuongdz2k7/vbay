# VBay Project Overview

VBay is a multi-module Maven project implementing a client-server auction system. It consists of a JavaFX client, a pure Java server, and a shared library for data transfer objects (DTOs) and common utilities.

## Architecture

The project follows a classic client-server architecture:
- **Client**: A JavaFX-based desktop application for users to browse auctions, place bids, and manage their profiles.
- **Server**: A backend service that manages the auction lifecycle, user authentication, bidding logic, and data persistence using JDBC.
- **Shared**: A common module containing shared contracts (DTOs), enums, and utility classes used by both the client and the server.

### Key Technologies
- **Java 25**: The primary programming language.
- **Maven**: Project management and build tool.
- **JavaFX 25**: Used for the client's graphical user interface.
- **AtlantaFX**: CSS theme library (PrimerLight) for styling the JavaFX UI.
- **MySQL**: The relational database used for data persistence.
- **Jackson**: For JSON serialization and deserialization.
- **Argon2**: For secure password hashing.
- **JUnit 5**: For unit and integration testing.

## Project Structure

- `client/`: Contains the JavaFX application code, FXML layouts, and client-side network logic.
- `server/`: Contains the backend logic, including services, repositories (JDBC), and socket-based request handling.
- `shared/`: Contains DTOs, enums (e.g., `RequestType`, `Event`), and utility classes like `JsonUtils` and `LoggingUtils`.

## Building and Running

### Prerequisites
- JDK 25 or higher.
- Maven 3.x.
- A running MySQL instance with the following default configuration (as seen in `DatabaseConfig.java`):
    - **Host**: `localhost`
    - **Port**: `1638`
    - **Database**: `vbay`
    - **Username**: `root`
    - **Password**: `1234`
- See `server/src/main/resources/data_init.sql` for the database schema.

### Commands

**Build all modules:**
```bash
mvn clean install
```

**Run the Server:**
```bash
mvn -pl server exec:java
```
The server listens on port `3618` by default.

**Run the Client:**
```bash
mvn -pl client javafx:run
```
*Note: If running the client from an IDE, add the VM option `--enable-native-access=javafx.graphics` for JavaFX 24+.*

## Development Conventions

- **Module Dependency**: Both `client` and `server` depend on the `shared` module. Always run `mvn install` on `shared` (or the root project) after making changes to it.
- **Network Protocol**: Communication between client and server is handled via TCP sockets using a custom `Request`/`Respond` protocol serialized as JSON.
- **Database Access**: The server uses the Repository pattern with JDBC for data access. Implementation classes are located in `com.vbay.server.repository.JDBCrepository`.
- **Security**: Never store passwords in plain text. Use the `Argon2PasswordHasher` provided in the `server` module.
- **Logging**: Use `com.vbay.shared.Utils.LoggingUtils` for consistent logging across the project.
- **Timezone**: The project is configured to use **UTC** timezone consistently across the server and database.

## Custom Agents

This project uses custom agents to assist with specialized tasks. These agents are defined in the `.gemini/agents/` directory.

- **Location**: `.gemini/agents/*.md`
- **Usage**: You can call an agent directly using the `@agent-name` syntax or let Gemini CLI automatically delegate tasks when needed.

### Available Agents:
- **vbay-architect**: Architectural consultant for coordinating changes across modules.
- **vbay-db**: Expert in SQL schema, JDBC repositories, and database optimization.
- **vbay-tester**: JUnit 5 expert focused on code quality and auction logic verification.
