# Photohaul Agent Guide

## Architecture Overview
Photohaul is a Java-based CLI for migrating photo collections with built-in deduplication.

- **`Main` / `Application`**: CLI entry point and process orchestration.
- **`model.Photo`**: Core data model with lazy-loaded metadata (EXIF).
- **`traversal`**: Recursive path walking and photo collection (`PathWalker`, `PhotoCollector`).
- **`deduplication`**: Multi-level strategy (Size -> Partial Hash -> Full SHA-256) to skip duplicates.
- **`migration`**: Strategy-based providers (Local, Dropbox, Google Drive, SFTP, S3) with delta tracking.
- **`resolution`**: Path resolution and organization logic (e.g., date-based folders).
- **`settings`**: Configuration management via `.properties` files.

## Common CLI Tasks
- **Build**: `./gradlew build` (Compiles, tests, and checks style/coverage)
- **Test**: `./gradlew test` (Runs JUnit 6 tests; requires 70%+ Jacoco coverage)
- **Run**: `./gradlew run -Dphotohaul.config=path/to/config.properties`
- **Lint**: `./gradlew checkstyleMain` (Enforces project code style)
- **Clean**: `./gradlew clean` (Removes build artifacts)

## Development Rules (IF-THEN)
- **IF** adding a new migration destination **THEN** implement `Migrator` and create a corresponding `MigratorFactory`.
- **IF** adding a new deduplication method **THEN** implement `DeduplicationStrategy` and register it in `PhotoDeduplicator`.
- **IF** modifying photo metadata extraction **THEN** update `Photo` model and ensure lazy-loading is preserved.
- **IF** adding dependencies **THEN** update `gradle/libs.versions.toml` and verify against existing API usage.
- **IF** coverage drops below 70% **THEN** add unit/integration tests in `src/test/java`.
- **IF** creating new configuration options **THEN** update `Settings.java` and provide an example in `src/main/resources/`.
