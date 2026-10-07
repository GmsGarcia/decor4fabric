[![](https://img.shields.io/discord/839440449147240489?color=5865F2&label=Discord&logo=Discord&logoColor=5865F2&style=for-the-badge)](https://discord.gg/VSgTpTGZ8A) [![](https://shields.io/badge/CurseForge-Click%20Here-F16436?logo=curseforge&style=for-the-badge&logoColor=F16436)](https://www.curseforge.com/minecraft/mc-mods/decor4fabric)

![lol](https://i.imgur.com/c55LQc6.png)

# 🔎About🔎
Decor4Fabric is a decoration mod that adds a lot of new furniture blocks to the game!
Most of the new blocks have special features!

🔨Craft a Carpentry Table to get started!🔨

![lol](https://i.imgur.com/kxeaqV5.png)
  
![lol](https://i.imgur.com/O7BseeC.png)
![lol](https://i.imgur.com/FzCj087.png)
![lol](https://i.imgur.com/OzmR0hz.png)

# Building

Requires **JDK 21 and 25**; the Gradle toolchain resolver downloads whichever
one is missing, so you do not need to install both by hand. Everything else
comes from the wrapper.

```bash
./gradlew build              # all four Minecraft versions, both loaders
./gradlew prismDoctor       # report the toolchain and mappings per target
./gradlew :26.1:fabric:runClient
```

| | 1.21.11 | 26.1 | 26.2 | 26.3 |
|---|---|---|---|---|
| Java | 21 | 25 | 25 | 25 |
| Fabric / NeoForge | yes | yes | yes | yes (NeoForge on beta) |

## ⚠️ Never run `clean build` as a single command

```bash
./gradlew clean      # ok
./gradlew build      # ok
./gradlew clean build   # BREAKS
```

Prism generates the NeoForge access transformer during **configuration** and
writes it under `versions/*/neoforge/build/tmp/neoformruntime/`. `clean` deletes
that file before `build` reads it, and every NeoForge target dies with
`NoSuchFileException: ..._accesstransformer.cfg`. Run them as two separate
invocations.

## Layout

```
build.gradle.kts      the ONLY build script
settings.gradle.kts   the ONLY settings script
common/               shared by all 4 versions — must not touch Minecraft
versions/<mc>/common/     one source tree per Minecraft version
versions/<mc>/fabric/     loader entrypoint + fabric.mod.json
versions/<mc>/neoforge/   loader entrypoint + neoforge.mods.toml
```

There are no `build.gradle.kts` files under `versions/`; Prism configures every
subproject from the root, and adding one can conflict with its own
configuration. Because each version has its own `common/`, common code is
duplicated four times — the duplication is deliberate and CI enforces that the
copies stay identical.

# Need Help?

You can join my [Discord Server](https://discord.gg/VSgTpTGZ8A) for quick help. You can also open an issue on [Issues](https://github.com/GmsGarcia/decor4fabric/issues) tab but i wont be so quick :/

![lol](https://i.imgur.com/G4VRGsZ.png)

# License

**Decor4Fabric** is licensed under the Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License. To view a copy of this license, visit [Creative Commons' website](http://creativecommons.org/licenses/by-nc-sa/4.0/) or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.


