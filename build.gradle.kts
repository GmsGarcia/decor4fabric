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
