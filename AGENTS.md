# Photohaul Codebase Overview for Agents

## Project Description

Photohaul is a Java-based CLI application for migrating photo collections to various destinations (local paths, Dropbox, Google Drive) with built-in deduplication to avoid uploading duplicates. It uses SHA-256 hashing for accurate duplicate detection and supports lazy metadata extraction for performance.

Main Class: `io.huangsam.photohaul.Main`

Build Tool: Gradle (with application plugin for CLI execution)

## Directory Structure (`src/`)

### Package `io.huangsam.photohaul`

- Main entry point for the CLI application.
- Handles argument parsing and configuration loading.
- Orchestrates photo collection, deduplication, and migration processes.

### Package `io.huangsam.photohaul.model`

- Defines core data models for photos.
- Implements lazy-loaded metadata extraction for performance.
- Provides builder patterns for photo instances.

### Package `io.huangsam.photohaul.deduplication`

- Implements multi-level photo deduplication logic.
- Uses size, partial hash, and full SHA-256 hashing for efficient duplicate detection.
- Processes photos in streams for parallel-friendly operations.

### Package `io.huangsam.photohaul.migration`

- Provides migration strategies and implementations.
- Supports transferring photos to local paths, Dropbox, Google Drive, and FTP servers.
- Includes factory patterns for configurability.

### Package `io.huangsam.photohaul.resolution`

- Handles photo path resolution and organization.
- Supports date-based folder structures for migrated photos.
- Provides interfaces for custom resolution logic.

### Package `io.huangsam.photohaul.settings`

- Manages configuration loading from properties files.
- Handles API keys and migration settings.
- Supports environment-specific configurations.

### `src/main/resources/`

- Default config files: `config.properties`, provider-specific examples (e.g., `dropbox-example.properties`).
- Static assets if needed.

### `src/test/java/io/huangsam/photohaul/`

- Unit tests for all major classes.
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
- **APIs**: Google Drive API, Dropbox SDK, Apache Commons Net for FTP.
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
