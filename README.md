# Photohaul

[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/huangsam/photohaul/ci.yml)](https://github.com/huangsam/photohaul/actions)
[![Code Coverage](https://img.shields.io/codecov/c/github/huangsam/photohaul)](https://codecov.io/gh/huangsam/photohaul)
[![License](https://img.shields.io/github/license/huangsam/photohaul)](https://github.com/huangsam/photohaul/blob/main/LICENSE)
[![GitHub Release](https://img.shields.io/github/v/release/huangsam/photohaul)](https://github.com/huangsam/photohaul/releases/latest)

Effortless photo management.

- Reorganize 10K+ photos (30 GB) in seconds!
- Migrate photos locally and to the cloud (Dropbox, Google Drive, SFTP, S3)
- Customize folder structures based on date, camera, and more
- Filter photos by file type, size, and other criteria
- Detect photo duplicates using modern hash techniques
- Skip unchanged files for faster subsequent runs

Say goodbye to photo clutter - 👋 + 🚀

![Sunny Bunny Tidy Up](images/sunny-bunny-tidy-up.webp)

## Motivation

As an avid photographer, I use Adobe Lightroom to organize my edited SLR
photos with custom file names and folder structures. This has worked well
for me since 2015, when I started getting serious about photography.

I want to apply those same patterns to old photos, so that it is easier
for me to access my precious memories. However, I struggle to apply the
same organization to my vast collection of older photos. I cannot apply
Lightroom settings to previously exported images and writing custom
scripts seems daunting.

I also want to migrate my photos over to a NAS or a cloud provider like
Google Drive, but it involves endless rounds of manual drag-and-drop
operations. I keep thinking to myself - is there a solution out there
that "just works" for my workflow?

## Value

Photohaul addresses the pain points above by providing a central hub for
photographers to filter, organize, and migrate photos to local storage
and cloud services. The folder structure for photos can be based on info
such as year taken and camera make.

## Getting started

For detailed instructions: [link](USERGUIDE.md)

**Install prerequisites:**

- Java 21 or later
- Gradle 9 or later

**Build application:**

- Run `./gradlew build` in your terminal

**Configure settings:**

- Set `PathRuleSet` to filter by extension, file size, etc.
- Set `MigratorMode` to `PATH` / `DROPBOX` / `GOOGLE_DRIVE` / `SFTP` / `S3`
- Set `PhotoResolver` to adjust folder structure
- Fill config file. Refer to examples in [src/main/resources](src/main/resources)

**Run application:**

- Run `./gradlew run` in your terminal
 - Optional: override with `-Dphotohaul.config=personal/path.properties` (classpath) or an absolute/relative filesystem path
	 (e.g., `-Dphotohaul.config=./src/main/resources/personal/path.properties`).

```text
> Task :run
08:05:14.518 [main] INFO io.huangsam.photohaul.Settings -- Use config file from photohaul.config: personal/path.properties
08:05:14.520 [main] INFO io.huangsam.photohaul.Settings -- Loaded settings from classpath: personal/path.properties
08:05:14.526 [main] DEBUG io.huangsam.photohaul.traversal.PathWalker -- Start traversal of /Users/samhuang/Pictures/Dummy PNG
08:05:14.536 [main] INFO io.huangsam.photohaul.deduplication.PhotoDeduplicator -- Deduplication complete: 6 unique photos, 0 duplicates removed
08:05:14.536 [main] DEBUG io.huangsam.photohaul.migration.PathMigrator -- Start path migration to /Users/samhuang/Pictures/Dummy FIN
08:05:14.627 [main] INFO io.huangsam.photohaul.Application -- Finish with success=6 failure=0

BUILD SUCCESSFUL in 535ms
3 actionable tasks: 1 executed, 2 up-to-date
```

**That's it!** Sit back and rediscover your memories! 😎 + 🍹 + 🌴

You're welcome 🙏
