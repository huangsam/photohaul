# Photohaul

Effortless photo management.

Can reorganize 10K+ photos (30 GB) in under 10 seconds!

Say goodbye to photo clutter - 👋 + 🚀

## Motivation

As an enthusiast photographer, I use Adobe Lightroom to organize my SLR photos
with custom file names and folder structures.

When I wanted to apply those same patterns to old photos, I ended up getting
frustrated with the tooling that exists to organize them.

If I also wanted to migrate my old photos and new photos over to a NAS or a cloud
storage provider like Dropbox or Google Drive, it would likely involve a bunch
of manual drag + drop operations.

Photohaul solves this problem by providing an automated solution to organize
and migrate your photo collections.

## Getting started

Prerequisites:

- Java 17+ installed on your system
- Gradle 8+ installed on your system
- Basic understanding of command line tools

Steps:

1. Run `gradle build` in your terminal to build the application
2. Set appropriate settings in `config.properties`
3. Customize additional parameters (Optional)
   - Set `PathRuleSet` to filter by extension, file size, etc.
   - Set `MigratorMode` to `PATH`, `DROPBOX` or `GOOGLE_DRIVE`
   - Set `PhotoResolver` to define how photos are organized
4. Run `gradle run` in your terminal to start the magic!

Then sit back and rediscover your memories! 😎 + 🍹 + 🌴

You're welcome 🙏
