# User guide

Here's an extended version of content from the [general README](README.md).

## General setup

If you have not built the application yet, please follow the general README
to get started first.

### Dropbox setup

Set mode to `DROPBOX` in `Main.java`

<img src="images/dbx-step-1.png" width="500" alt="Dropbox step 1"/>

Configure Dropbox credentials in `config.properties`

<img src="images/dbx-step-2.png" width="250" alt="Dropbox step 2"/>

### Google Drive setup

Set mode to `GOOGLE_DRIVE` in `Main.java`

<img src="images/drive-step-1.png" width="500" alt="Drive step 1"/>

Configure Google Drive credentials in `config.properties`

<img src="images/drive-step-2.png" width="250" alt="Drive step 2"/>

## Run migration

- Open your terminal and navigate to the `./photohaul` directory
- Run the command `gradle run`. This will start the migration to Google Drive

<img src="images/migration-step.png" width="1000" alt="Migration step"/>

## Validate migration

Once the migration is complete, you can verify that your photos have been uploaded
to Google Drive. Here's an example of how I validate the output for Google Drive.

Folder creation

<img src="images/validate-step-1.png" width="1000" alt="Validate step 1"/>

Photo creation in 2015

<img src="images/validate-step-2.png" width="500" alt="Validate step 2"/>

Photo creation in 2024

<img src="images/validate-step-3.png" width="500" alt="Validate step 3"/>
