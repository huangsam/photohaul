# Photohaul Codebase Overview for Agents

## Project Description

Photohaul is a Java-based CLI application for migrating photo collections to various destinations (local paths, Dropbox, Google Drive) with built-in deduplication to avoid uploading duplicates. It uses SHA-256 hashing for accurate duplicate detection and supports lazy metadata extraction for performance.

Main Class: `io.huangsam.photohaul.Main`

Build Tool: Gradle (with application plugin for CLI execution)

## Directory Structure (`src/`)

### `src/main/java/io/huangsam/photohaul/`

- **`Main.java`**: Entry point for the CLI app. Parses arguments, loads config, and orchestrates photo collection, deduplication, and migration.
- **`model/`**:
  - `Photo.java`: Immutable photo representation with lazy-loaded metadata (e.g., EXIF data like taken time, camera make/model). Uses double-checked locking for thread-safe loading.
  - `PhotoBuilder.java`: Builder pattern for creating Photo instances (used in tests or special cases).
- **`deduplication/`**:
  - `PhotoDeduplicator.java`: Core logic for multi-level deduplication (size → partial hash → full SHA-256). Processes photos in streams for efficiency.
- **`migration/`**:
  - `Migrator.java`: Interface for migration strategies.
  - `PathMigrator.java`: Migrates to local file system paths (move/copy/dry-run).
  - `DropboxMigrator.java`: Uploads to Dropbox using their SDK.
  - `GoogleDriveMigrator.java`: Uploads to Google Drive via API.
  - `MigratorFactory.java`: Factory for creating migrators based on config.
- **`resolution/`**:
  - `PhotoResolver.java`: Interface for resolving photo paths/folders (e.g., by date).
  - Implementations like `DateResolver.java` for organizing by year/month.
- **`settings/`**: Configuration loading from properties files (e.g., API keys, paths).

### `src/main/resources/`

- Default config files: `config.properties`, provider-specific examples (e.g., `dropbox-example.properties`).
- Static assets if needed.

### `src/test/java/io/huangsam/photohaul/`

- Unit tests for all major classes (e.g., `TestPhoto.java`, `TestPhotoDeduplicator.java`).
- Integration tests for end-to-end flows.
- Uses JUnit 5, Mockito for mocking.

## Build Configuration (`build.gradle`)

### Plugins

- `java`: Standard Java compilation.
- `application`: Generates CLI scripts and handles main class execution.
- `checkstyle`: Code style enforcement.
- `jacoco`: Code coverage reporting (70% minimum).
- `task-tree`: For visualizing task dependencies.

### Dependencies (via `gradle/libs.versions.toml`)

- **Core**: JetBrains Annotations, SLF4J/Logback for logging.
- **APIs**: Google Drive API, Dropbox SDK for cloud migrations.
- **Metadata**: Drew Noakes' metadata-extractor for EXIF data.
- **Testing**: JUnit 5, Mockito.

### Application Block

- Main class: `io.huangsam.photohaul.Main`
- JVM Args: `-Xmx1g` (1GB heap), `-XX:+UseG1GC` (G1 garbage collector) for performance tuning in photo processing.

### Tasks

- `run`: Executes the app, forwards `-Dphotohaul.config` system property for custom configs.
- `test`: Runs JUnit tests with Jacoco coverage.
- `check`: Includes coverage verification.
- `build`: Compiles, tests, and packages.

### Key Notes for Agents

- **Performance Focus**: Recent optimizations include lazy metadata loading and multi-level deduplication to reduce I/O.
- **Thread Safety**: Photo metadata uses synchronized lazy loading; deduplication is stream-based and parallel-friendly.
- **Config-Driven**: Behavior changes via properties files (e.g., migration type, API credentials).
- **Error Handling**: Robust logging with SLF4J; exceptions in metadata/migration are caught and logged without failing the whole process.
- **Testing**: High coverage; mocks external APIs (Dropbox, Drive) for reliable tests.

For contributions or modifications, ensure tests pass and coverage stays above 70%.

Use `./gradlew run -Dphotohaul.config=path/to/config.properties` for local testing.
