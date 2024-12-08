# Photohaul

Effortless photo management.

- Reorganize 10K+ photos (30 GB) in under 10 seconds!
- Migrate photos locally and to the cloud

Say goodbye to photo clutter - 👋 + 🚀

![Sunny Bunny Tidy Up](sunny-bunny-tidy-up.webp)

## Motivation

As an avid photographer, I use Adobe Lightroom to organize my edited SLR
photos with custom file names and folder structures. This has worked well
for me since 2015, when I started getting serious about photography.

I want to apply those same patterns to old photos, so that it is easier
for me to access my precious memories. Unfortunately, Lightroom settings
cannot apply to photos after they are exported. And writing bespoke
scripts to manage these photos is no fun!

If I also want to migrate my photos over to a NAS or a cloud provider
like Google Drive, it typically involves a bunch of manual drag + drop
operations. After a few dozen times, I get tired of doing the same thing
over and over again.

## Value

Photohaul addresses the pain points above by providing a central hub to
organize and migrate photo collections. It lets photographers choose the
photos that they will organize. It gives them the freedom to customize
the folder structure of their photos by dimensions such as year taken
and camera make. Last but not least, it allows them to choose the medium
they want to store photos on: local, Dropbox, Google Drive.

## Getting started

Prerequisites:

- Java 17+ installed on your system
- Gradle 8+ installed on your system
- Basic understanding of command line tools

Steps:

1. Run `gradle build` in your terminal to build the application
2. Set `PathRuleSet` to filter by extension, file size, etc.
3. Set `MigratorMode` to `PATH`, `DROPBOX` or `GOOGLE_DRIVE`
4. Set `PhotoResolver` to define how photos are organized
5. Set appropriate parameters in `config.properties`
6. Run `gradle run` in your terminal to start the magic!

Then sit back and rediscover your memories! 😎 + 🍹 + 🌴

You're welcome 🙏
