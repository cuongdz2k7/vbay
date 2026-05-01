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

On JDK 24+ / 25, JavaFX may print a restricted native access warning if the
runtime is started without the required VM flag. This project configures that
flag for `mvn -pl client javafx:run`.

If you run the client directly from your IDE instead of Maven, add this VM
option to the run configuration:
```text
--enable-native-access=javafx.graphics
```

## Notes
- `shared` contains DTOs/contracts used by both client and server.
- `server` is pure Java backend starter.
- `client` is JavaFX starter with FXML.
