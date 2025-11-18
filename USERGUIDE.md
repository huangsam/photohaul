# User guide

Here's an extended version of content from the [general README](README.md).

## General setup

If you have not built the application yet, please follow the general README
to get started first.

You can specify a different configuration file for various environments or specific scenarios by passing a JVM system property.
Use the `-Dphotohaul.config=<classpath-resource-or-filesystem-path>` flag when running the application. The value can be either the classpath resource name (relative to `src/main/resources`) or a filesystem path.

Examples:

```shell
# Use the default file (no override)
./gradlew run

# Override with a classpath resource under src/main/resources/personal
./gradlew run -Dphotohaul.config=personal/path.properties

# Or override with a filesystem path (absolute or relative)
./gradlew run -Dphotohaul.config=./src/main/resources/personal/path.properties
```

Notes:

- You can use either a classpath name (`personal/path.properties`) or a filesystem path.
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

### SFTP setup

Configure the following property fields:

- `migrator.mode`
- `path.source`
- `sftp.host`
- `sftp.port` (optional, defaults to 22)
- `sftp.username`
- `sftp.password`
- `sftp.target`

SFTP (SSH File Transfer Protocol) is used for secure file transfers over SSH. Host keys are verified against your known hosts file for security. Ensure your server supports SFTP.

### S3 setup

Configure the following property fields:

- `migrator.mode`
- `path.source`
- `s3.bucket`
- `s3.accessKey`
- `s3.secretKey`
- `s3.region` (optional, defaults to us-east-1)

Amazon S3 (Simple Storage Service) is used for scalable cloud storage. You'll need AWS credentials with S3 permissions. [Click here](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/get-started.html) to learn how to obtain and configure AWS credentials.

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
