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
like Google Drive, it typically involves many drag + drop operations.
After a few dozen times, I get tired of doing it again and again.

## Value

Photohaul addresses the pain points above by providing a central hub
for photographers to filter, organize, and migrate photos to local
storage or cloud services. The folder structure for photos can be based
on info such as year taken and camera make.

## Getting started

Prerequisites:

- Java 17+ installed on your system
- Gradle 8+ installed on your system
- Basic understanding of command line tools

Steps:

1. Run `gradle build` in your terminal to build the application
2. Set `PathRuleSet` to filter photos by extension, file size, etc.
3. Set `MigratorMode` to `PATH`, `DROPBOX` or `GOOGLE_DRIVE`
4. Set `PhotoResolver` to adjust folder structure for photos
5. Set `Settings` to your properties file in `src/main/resources`
6. Fill relevant parameters in your properties file
7. Run `gradle run` in your terminal to start the magic!

Then sit back and rediscover your memories! 😎 + 🍹 + 🌴

You're welcome 🙏
