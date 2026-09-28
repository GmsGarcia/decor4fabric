pluginManagement {
    repositories {
        // Prism's own maven MUST be first. GradlePluginPortal does not host
        // dev.prism.*, so putting it later resolves nothing and the build
        // fails with a bare "plugin not found" that names the wrong cause.
        maven { url = uri("https://maven.leclowndu93150.dev/releases") }
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.neoforged.net/releases") }
        // Prism pulls net.minecraftforge.gradle:ForgeGradle onto the settings
        // classpath, and that artifact is only reachable through Sponge's
        // mirror. Without this the build dies at configuration time with
        // "Could not find net.minecraftforge.gradle:ForgeGradle" before a
        // single target is set up.
        maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
    }
}

plugins {
    // Downloads the JDKs the targets ask for. Java 21 and 25 are both required
    // (see javaVersion in build.gradle.kts) and no single local install covers
    // them on most machines, so without this the toolchain lookup fails
    // before a single line of mod code is compiled.
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
    // Pinned, never "+". A dynamic version would silently change the build
    // for anyone who clones later, and Prism 0.6.x pins Loom and ModDevGradle
    // transitively, so bumping Prism changes those underneath us.
    id("dev.prism.settings") version "0.6.0"
}

rootProject.name = "decor4fabric"

prism {
    // Root common/ holds code shared across ALL four Minecraft versions, so it
    // must not reference any Minecraft class. It is currently documentation
    // only. See common/README.md.
    sharedCommon()

    // One target per Minecraft MINOR line, never per patch. Patches within a
    // line share a pack format, so a single jar serves all of them:
    //   26.1 covers 26.1, 26.1.1, 26.1.2
    // Minors do not share a pack format and each needs its own pack.mcmeta,
    // so never add a 26.1.2 or 26.2.1 target.
    version("1.21.11") { common(); fabric(); neoforge() }
    version("26.1") { common(); fabric(); neoforge() }
    version("26.2") { common(); fabric(); neoforge() }
    version("26.3") { common(); fabric(); neoforge() }
}
