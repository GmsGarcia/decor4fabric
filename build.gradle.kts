plugins {
    id("dev.prism")
}

group = "net.gmsgarcia.decor4fabric"
// 2.0.0, not 1.3: the loader and Minecraft matrix is a breaking change for
// anyone holding a world or datapack built against 1.2. The 1.18.2 line stays
// frozen on main and tagged v1.2-1.18.2.
version = "2.0.0"

prism {
    metadata {
        modId = "decor4fabric"
        name = "Decor4Fabric"
        description = "A decoration mod with furniture blocks that mostly have special features."
        // Matches the LICENSE file and the README. The 1.18.2 fabric.mod.json
        // said "cc-by-sa-4.0", which dropped the NonCommercial term and
        // contradicted both; corrected here.
        license = "CC-BY-NC-SA-4.0"
        author("GmsGarcia#1553")
        // Prism appends the version itself, so {version} is deliberately
        // absent here. Including it yields "2.0.0-2.0.0".
        archivesName = "decor4fabric-{mc}-{loader}"
        expand("homepage", "http://gmsgarcia.ga/decor")
        expand("sources", "https://github.com/GmsGarcia/decor4fabric")
    }

    // There is no yarn(...) call anywhere in this file, and that omission is
    // load-bearing: it is what selects Mojmap. The 1.18.2 build named
    // "net.minecraft.class_XXXX" style yarn classes throughout, and every one
    // of those files has to be rewritten against Mojang names.

    // SUPPORT POLICY: one jar per Minecraft MINOR line, covering every patch
    // in it. Three places have to agree on that line and only two are here:
    //   1. minecraftVersions(...)  publishing list, a hardcoded list of exact
    //      version strings that must be extended by hand when a patch ships.
    //      Nothing else fails loudly if it is missed; the release just becomes
    //      unfindable.
    //   2. fabric.mod.json         ">=26.1 <26.2"
    //   3. neoforge.mods.toml      minecraft "[26.1,26.2)"
    // Bounds are per loader because Fabric rejects Maven range syntax: use
    // ">=" and "<" with a space, never "[1.21,2)". A bare "26.1" would be an
    // exact match on Fabric, and an uncapped ">=26.1" would leak into 26.2.x.

    version("1.21.11") {
        accessWidener("versions/1.21.11/common/src/main/resources/decor4fabric.accesswidener")
        javaVersion = 21
        minecraftVersions("1.21.11")
        fabric {
            loaderVersion = "0.19.5"
            fabricApi("0.141.6+1.21.11")
        }
        neoforge { loaderVersion = "21.11.45" }
    }

    version("26.1") {
        accessWidener("versions/26.1/common/src/main/resources/decor4fabric.classtweaker")
        javaVersion = 25
        // 26.1.0/26.1.1/26.1.2 share pack format 84.0 / 101.1, so one jar
        // serves all three and nothing is rebuilt when a patch ships.
        minecraftVersions("26.1", "26.1.1", "26.1.2")
        fabric {
            loaderVersion = "0.19.5"
            fabricApi("0.155.3+26.1.2")
        }
        neoforge {
            // Compile target only, deliberately the newest patch in the line so
            // the jar is built against the most recent 26.1 API. This is NOT
            // the runtime floor: that is a literal "[26.1,)" in
            // neoforge.mods.toml, because a floor of "[26.1.2.112,)" would
            // lock out 26.1.0 and 26.1.1 whose NeoForge builds sort below it.
            loaderVersion = "26.1.2.112"
        }
    }

    // 26.2. Pack format moves 84.0/101.1 -> 88.0/107.1, so this needs its own
    // pack.mcmeta and its own common tree. NeoForge is stable here.
    version("26.2") {
        accessWidener("versions/26.2/common/src/main/resources/decor4fabric.classtweaker")
        javaVersion = 25
        minecraftVersions("26.2")
        fabric {
            loaderVersion = "0.19.5"
            fabricApi("0.161.0+26.2")
        }
        neoforge {
            loaderVersion = "26.2.0.88"
        }
    }

    // 26.3. Pack format 97.1/121.0.
    //
    // CAVEAT: 26.3 shipped 2026-09-15 and NeoForge has not cut a stable build
    // for it yet, so this target compiles against a beta loader. Treat it as
    // provisional: the floor in neoforge.mods.toml is a literal "[26.3,)" so a
    // future 26.3.0.99 drops in with no metadata change, but do not publish
    // this jar as stable support.
    version("26.3") {
        accessWidener("versions/26.3/common/src/main/resources/decor4fabric.classtweaker")
        javaVersion = 25
        minecraftVersions("26.3")
        fabric {
            loaderVersion = "0.19.5"
            fabricApi("0.161.0+26.3")
        }
        neoforge {
            loaderVersion = "26.3.0.26-beta"
        }
    }
}

/**
 * Puts `src/main/generated/resources` on the jar for every version.
 *
 * <p>Prism does not add this directory on its own, and its absence is silent:
 * the build succeeds, the hand-authored models ship, and every one of the
 * thousand-odd generated blockstates, models, item definitions, loot tables,
 * tags and the language file are simply absent from the jar. A client then
 * shows the purple-and-black missing model for every block, and a server test
 * passes because the server never loads a model. This is the one line that
 * makes Phase 5's output reachable, so it is stated here rather than left to
 * each version's synthesised script.
 *
 * <p>It is a plain `srcDir` and not a `generatedBy` provider, so the jar
 * builds without running the generator first; the trade is a stale tree
 * compiles silently, which is why `generateAllResources` is a task in its own
 * right rather than something wired into `build`.
 */
listOf("1.21.11", "26.1", "26.2", "26.3").forEach { mc ->
    val common = project(":$mc:common")
    // The root script is evaluated before Prism has applied the Java plugin to
    // the synthesised subprojects, so SourceSetContainer does not exist yet at
    // this point. withId fires the action immediately if java is already
    // applied and otherwise when it is, which is the only way to touch a
    // subproject's extensions from here without an afterEvaluate.
    common.plugins.withId("java") {
        common.extensions.getByType<SourceSetContainer>().getByName("main")
            .resources.srcDir("src/main/generated/resources")
    }
}

/**
 * Registers `generateResources` on every ported version's `common` project.
 *
 * <p>Prism synthesises each `versions/<mc>/build.gradle.kts` at configuration
 * time, so there is no per-version script to hang a task off. Registering the
 * task *on the common project* rather than on the root is the load-bearing
 * detail: a root-project `JavaExec` that reaches across into
 * `:26.1:common:runtimeClasspath` fails under Gradle 9 with
 * "Current thread does not hold the state lock for project ':26.1:common'".
 * Resolving another project's configuration is only legal from inside a task
 * that Gradle has locked, and owning the project is the simplest way to be that
 * task. It also drops the need for a `-Pdecor.mc` property: each version's task
 * knows its own working directory, so `./gradlew generateAllResources` covers
 * all three in one pass.
 *
 * <p>The generator is a plain `main()` rather than a loader datagen task on
 * purpose. Its output is committed to `src/main/generated/resources` and is
 * byte-identical across all three ported versions, so it must not depend on a
 * loader's resource-pack layout or on that version's vanilla asset tree.
 *
 * <p>{@code 26.3} is deliberately absent. It is still a buildable stub, and
 * registering a generator there would create a `main()` referencing a
 * catalogue the stub does not have.
 *
 * <p>The Java version per line is spelled out here rather than read from the
 * `prism { version(...) }` blocks, because `javaVersion` is a local inside
 * Prism's `version` function and is not readable from the root script. Keep
 * this table in step with the `javaVersion = ` lines above; running a 1.21.11
 * generator on JDK 25 would still work but would not prove the sources compile
 * at the target level.
 */
val generatorTargets = listOf(
    "1.21.11" to 21,
    "26.1" to 25,
    "26.2" to 25,
)

generatorTargets.forEach { (mc, javaVersion) ->
    val common = project(":$mc:common")
    common.tasks.register<JavaExec>("generateResources") {
        group = "decor4fabric"
        description = "Regenerates src/main/generated/resources for Minecraft $mc."

        val java = common.extensions.getByType<SourceSetContainer>()["main"]
        mainClass.set("net.gmsgarcia.decor4fabric.generator.ResourceGenerator")
        classpath = files(java.runtimeClasspath, java.output)
        // The generator writes relative to the working directory, so this is
        // what keeps each version's output in its own tree. It has to be
        // common.projectDir and not this file's `projectDir`: inside the
        // forEach the receiver is still the root project, whose projectDir is
        // the repository root. Getting that wrong writes the whole tree to
        // <repo>/src/main/generated/resources and the version's own copy
        // silently stays stale.
        workingDir = common.projectDir
        javaLauncher.set(
            common.extensions
                .getByType<JavaToolchainService>()
                .launcherFor { languageVersion.set(JavaLanguageVersion.of(javaVersion)) }
        )
    }
}

/**
 * Runs every ported version's generator in catalogue order.
 *
 * <p>The versions are ordered rather than parallel so a diff that appears in
 * one tree is visible in the next, instead of three tasks racing to write files
 * that are meant to match.
 */
val generateAllResources = tasks.register("generateAllResources") {
    group = "decor4fabric"
    description = "Regenerates resources for 1.21.11, 26.1 and 26.2 in sequence."
    dependsOn(generatorTargets.map { (mc, _) -> "$mc:common:generateResources" })
}
