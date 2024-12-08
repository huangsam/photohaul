# Photohaul

Effortless photo management.

- Reorganize 10K+ photos (30 GB) in under 10 seconds!
- Migrate photos locally and to the cloud

Say goodbye to photo clutter - 👋 + 🚀

![Sunny Bunny Tidy Up](sunny-bunny-tidy-up.webp)

## Motivation

As an enthusiast photographer, I use Adobe Lightroom to organize my edited
SLR photos with custom file names and folder structures. This has worked
well for me since 2015, when I started getting serious about photography.

I want to apply those same patterns to old photos, so that it would be
easier for me to access my precious memories. Unfortunately, Lightroom
file and folder settings cannot apply to photos after they have been
exported. And writing bespoke scripts for these photos is no fun!

If I also want to migrate my photos over to a NAS or a cloud provider
like Dropbox or Google Drive, it typically involves a bunch of manual
drag + drop operations. After a few dozen times, I get tired of doing
the same thing on my browser over and over again.

Photohaul solves these problems by providing an automated solution to
organize and migrate your photo collections.

## Getting started

Prerequisites:

- Java 17+ installed on your system
- Gradle 8+ installed on your system
- Basic understanding of command line tools

Steps:

1. Run `gradle build` in your terminal to build the application
2. Set appropriate settings in `config.properties`
3. Set `PathRuleSet` to filter by extension, file size, etc.
4. Set `MigratorMode` to `PATH`, `DROPBOX` or `GOOGLE_DRIVE`
5. Set `PhotoResolver` to define how photos are organized
6. Run `gradle run` in your terminal to start the magic!

Then sit back and rediscover your memories! 😎 + 🍹 + 🌴

You're welcome 🙏
