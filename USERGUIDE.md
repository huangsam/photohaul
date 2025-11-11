# User guide

Here's an extended version of content from the [general README](README.md).

## General setup

If you have not built the application yet, please follow the general README
to get started first.

You can specify a different configuration file for various environments or specific scenarios by passing a JVM system property.
Use the `-Dphotohaul.config=<classpath-resource>` flag when running the application. The value must be the classpath resource name (relative to `src/main/resources`).

Examples:

```shell
# Use the default file (no override)
./gradlew run

# Override with a custom resource under src/main/resources/personal
./gradlew run -Dphotohaul.config=personal/path.properties
```

Notes:

- Use classpath name (`personal/path.properties`), not a filesystem path.
- Default is `config.properties` if not overridden.

### Path setup

Configure the following property fields:

- `migrator.mode`
- `path.source`
- `path.target`
- `path.action`

Refer to `PathMigrator` to learn more about the `path.action` values.

### Dropbox setup

Configure the following property fields:

- `migrator.mode`
- `path.source`
- `dbx.target`
- `dbx.clientId`
- `dbx.accessToken`

[Click here](https://github.com/dropbox/dropbox-sdk-java?tab=readme-ov-file#dropbox-for-java-tutorial) to learn how to setup the `dbx` fields.

### Google Drive setup

Configure the following property fields:

- `migrator.mode`
- `path.source`
- `drive.target`
- `drive.credentialFile`
- `drive.appName`

[Click here](https://developers.google.com/drive/api/quickstart/java#set-up-environment) to learn how to setup the `drive` fields.

## Run migration

- Open your terminal and navigate to the `./photohaul` directory
- Run the command `./gradlew run`. This will start the migration process

## Validate migration

Once the migration is complete, you can verify that your photos are uploaded successfully.

Below is an example of validating changes on Google Drive.

### Google Drive validation

**Folder creation** was successful:

<img src="images/validate-step-1.png" width="800" alt="Validate step 1"/>

Photo creation in **2015** was successful:

<img src="images/validate-step-2.png" width="700" alt="Validate step 2"/>

Photo creation in **2024** was successful:

<img src="images/validate-step-3.png" width="700" alt="Validate step 3"/>
