# Photohaul

[![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/huangsam/photohaul/ci.yml)](https://github.com/huangsam/photohaul/actions)
[![Code Coverage](https://img.shields.io/codecov/c/github/huangsam/photohaul)](https://codecov.io/gh/huangsam/photohaul)
[![License](https://img.shields.io/github/license/huangsam/photohaul)](https://github.com/huangsam/photohaul/blob/main/LICENSE)
[![GitHub Release](https://img.shields.io/github/v/release/huangsam/photohaul)](https://github.com/huangsam/photohaul/releases/latest)

Effortless photo management.

- Reorganize 10K+ photos (30 GB) in seconds!
- Migrate photos locally and to the cloud (Dropbox, Google Drive)
- Customize folder structures based on date, camera, and more
- Filter photos by file type, size, and other criteria

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
- Set `MigratorMode` to `PATH`, `DROPBOX` or `GOOGLE_DRIVE`
- Set `PhotoResolver` to adjust folder structure
- Fill config file. Refer to examples in [src/main/resources](src/main/resources)

**Run application:**

- Run `./gradlew run` in your terminal
 - Optional: override with `-Dphotohaul.config=personal/path.properties` (classpath) or an absolute/relative filesystem path
	 (e.g., `-Dphotohaul.config=./src/main/resources/personal/path.properties`).

```text
> Task :run
20:05:18.533 [main] INFO io.huangsam.photohaul.Settings -- Use config file from photohaul.config: config.properties
20:05:18.538 [main] DEBUG io.huangsam.photohaul.traversal.PathWalker -- Start traversal of /Users/samhuang/Pictures/Dummy PNG
20:05:18.768 [main] DEBUG io.huangsam.photohaul.migration.GoogleDriveMigrator -- Start Drive migration to 10uRd6...
20:05:23.145 [main] INFO io.huangsam.photohaul.Main -- Finish with success=6 failure=0

BUILD SUCCESSFUL in 5s
3 actionable tasks: 1 executed, 2 up-to-date
```

**That's it!** Sit back and rediscover your memories! 😎 + 🍹 + 🌴

You're welcome 🙏
