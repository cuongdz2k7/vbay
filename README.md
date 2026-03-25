# vbay

Skeleton multi-module Maven project for:
- `shared` -> package `com.vbay.shared`
- `server` -> package `com.vbay.server`
- `client` -> package `com.vbay`

## Commands

Build all modules:
```bash
mvn clean install
```

Run server:
```bash
mvn -pl server exec:java
```

Run JavaFX client:
```bash
mvn -pl client javafx:run
```

## Notes
- `shared` contains DTOs/contracts used by both client and server.
- `server` is pure Java backend starter.
- `client` is JavaFX starter with FXML.
