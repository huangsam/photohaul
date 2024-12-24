# User guide

Here's an extended version of content from the [general README](README.md).

## General setup

If you have not built the application yet, please follow the general README
to get started first.

### Path setup

Set mode to `PATH` in `Main.java`

<img src="images/path-step-1.png" width="500" alt="Path step 1"/>

Configure source, target in `config.properties`

<img src="images/path-step-2.png" width="250" alt="Path step 2"/>

### Dropbox setup

Set mode to `DROPBOX` in `Main.java`

<img src="images/dbx-step-1.png" width="500" alt="Dropbox step 1"/>

Configure source, target, credentials in `config.properties`

<img src="images/dbx-step-2.png" width="250" alt="Dropbox step 2"/>

[Click here](https://github.com/dropbox/dropbox-sdk-java?tab=readme-ov-file#dropbox-for-java-tutorial) to learn more.

### Google Drive setup

Set mode to `GOOGLE_DRIVE` in `Main.java`

<img src="images/drive-step-1.png" width="500" alt="Drive step 1"/>

Configure source, target, credentials in `config.properties`

<img src="images/drive-step-2.png" width="250" alt="Drive step 2"/>

[Click here](https://developers.google.com/drive/api/quickstart/java#set-up-environment) to learn more.

## Run migration

- Open your terminal and navigate to the `./photohaul` directory
- Run the command `gradle run`. This will start the migration to Google Drive

<img src="images/migration-step.png" width="800" alt="Migration step"/>

## Validate migration

Once the migration is complete, you can verify that your photos have been uploaded
to Google Drive. Here's an example of how I validate the output for Google Drive.

**Folder creation** was successful:

<img src="images/validate-step-1.png" width="800" alt="Validate step 1"/>

Photo creation in **2015** was successful:

<img src="images/validate-step-2.png" width="700" alt="Validate step 2"/>

Photo creation in **2024** was successful:

<img src="images/validate-step-3.png" width="700" alt="Validate step 3"/>
