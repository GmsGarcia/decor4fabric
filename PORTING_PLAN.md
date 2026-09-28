# Decor4Fabric — Refactor & Port Plan (1.18.2 → 1.21.11 / 26.x, Fabric + NeoForge)

Working notes for reviving a mod that has been untouched since 2022-05-16
(last commit `ff07128`). The reference architecture is the sibling repo
`../compress-em`, which took a 1.17 mod to four MC targets × two loaders with
Prism. Everything below that says "copy compress-em" means a file path in that
repo.

---

## 0. Executive summary

| | |
|---|---|
| **Current state** | 1.18.2, Fabric only, Yarn 1.18.2, Loom `0.11-SNAPSHOT`, Java 17. 26 Java files (3,204 lines), 815 resource files. Zero mixins. |
| **Proposed state** | Prism multi-version project: 1.21.11, 26.1, 26.2, 26.3 × {Fabric, NeoForge}. Mojmap. Java 21 / 25. **166 blocks** (121 preserved + 45 from cherry/mangrove/pale oak). CC BY-NC-SA 4.0. |
| **Decisions** | All four settled 2026-09-28 — see §3. Headline: **no mixins anywhere in the port.** |
| **Loader seam** | One interface (`ContentRegistrar`) taking `Supplier<T>`, two impls. Plus a ~25-line `VersionCheckRegistrar` for the play-phase version ping (§3.1.1). |
| **Content refactor** | `blockRegistry`'s 121 hand-written `static final Block` fields, 121 `BlockItem` constructions, 2 `BlockEntityType`s and 3 item groups — all inside 595 lines of `Registry.register` — collapse into a `BlockSpec` record + a table, exactly as compress-em did with its 130 blocks. |
| **Resource refactor** | 815 hand-written JSON → one checked-in generator, ~400 lines of provider code. Now emits ~1,085 files (815 + ~270 for the 45 new blocks). |
| **Biggest single risk** | `workBenchScreen` is a near-verbatim fork of vanilla `StonecutterScreen`, and the entire GUI render path was replaced in 1.20 and again in 1.20.5. Budget for a rewrite, not a port. **In scope** — decision 3.2 chose option A. |
| **Second risk** | The version-lock handshake is gone (§3.1.1), but the workbench *recipe* is a rewrite too: `CuttingRecipe` no longer exists, and the handler must implement `RecipeBookMenu` or the recipes are unreachable. |
| **Not a port risk** | Assets. 22 PNGs (3 workbench, 18 item, 1 GUI). Everything else reuses vanilla wood textures, which is why 45 new blocks cost zero new art. |
| **Estimated effort** | Phase 1: ~1 day. Phases 2–4 (the real work): ~6–10 days — up from 4–7, because the workbench stays in scope and 45 blocks join the table. Phases 5–7: ~2–3 days. |

---

## 1. Ground truth: what is actually in the repo

Verified by reading the tree, not inferred from the README.

### 1.1 Build

`gradle.properties`:
```
minecraft_version=1.18.2
yarn_mappings=1.18.2+build.3
loader_version=0.13.3
fabric_version=0.51.1+1.18.2
mod_version = 1.2
maven_group = net.gmsgarcia.decor4fabric
```
`build.gradle`: `fabric-loom` `0.11-SNAPSHOT`, `JavaVersion.VERSION_17`, one dead
repository (`maven.shedaniel.me`), `withSourcesJar()`, an empty `publishing`
block. Gradle wrapper is **7.3**.

`fabric.mod.json` declares three entrypoints (`main`, `client`, `server`), an
**empty** `mixins` array, `"minecraft": "1.18.2"`, and `license: cc-by-sa-4.0`.

`gradle.properties` also contains leading tabs on every property line — harmless,
but delete them while you're in there.

### 1.2 Java (26 files, 3,204 lines, all in `src/main/java`)

```
mainDecor.java (65)          ModInitializer; calls every registerX() in order;
                             getModVersion() does Float.parseFloat (line 58)
clientDecor.java (67)        ClientModInitializer; screen, entity renderer, login net
serverDecor.java (52)        DedicatedServerModInitializer; login version check
registry/
  blockRegistry.java (595)   121 Block fields + 121 BlockItems + 2 BlockEntityTypes
                             + 3 FabricItemGroupBuilder tabs; ALL in class-init
  tagRegistry.java (24)      5 TagKey<Block>
  recipeRegistry.java (19)   1 RecipeType + 1 RecipeSerializer
  screenRegistry.java (15)   ScreenHandlerRegistry.registerSimple
  fuelRegistry.java (11)     FuelRegistryImpl.INSTANCE.add  ← internal Fabric API
  itemRegistry.java (8)      empty, never called
blocks/
  logBench.java (276)        HorizontalFacingBlock + BlockEntityProvider + Waterloggable;
                             AXE_TYPE IntProperty 0..6; whole axe-place/take-back flow
  logBench2.java (307)       variant of the above; own AXE_TYPE with the SAME name
  logBench3.java (168)       variant (high bench); no AXE_TYPE at all
  logSmallStool.java (306)   variant (BE, 1 slot); WOOL_COLOR 0..16, 0 = "no carpet"
  logChair.java (184)        ctor takes a boolean `hasArmRests`
  logTable.java (162)        plain block
  workBench.java (126)       Waterloggable + NamedScreenHandlerFactory, 9-part VoxelShape;
                             the only Material.METAL block (all others are WOOD)
  logFence.java (16)         extends FenceBlock
  logFenceGate.java (16)     extends FenceGateBlock
  block_entities/
    logBench_BlockEntity.java (36)         1-slot inventory, readNbt/writeNbt
    logSmallStool_BlockEntity.java (36)    same
  inventories/impl_Inventory.java (78)     Inventory default-methods helper
screen/
  workBenchScreenHandler.java (209)        fork of StonecutterScreenHandler
  workBenchScreen.java (174)               fork of StonecutterScreen
recipes/workBenchRecipe.java (75)          extends CuttingRecipe; custom Serializer
sitOnStuff/
  Sit.java (108)             UseBlockCallback; 4 near-identical branches;
                             SIT_ENTITY_TYPE registered in a static initializer
  SitEntity.java (71)        marker entity; public static HashMap<Vec3d,BlockPos> OCCUPIED
```

### 1.3 Resources (815 files)

```
assets/decor4fabric/
  blockstates/            121   (all multipart; string-typed `when` values: "north":"true")
  models/block/           122   + models/ 19 abstract parents
    fences/ 24  fence_gates/ 32  stripped_fences/ 24  stripped_fence_gates/ 32
    repetitive_models/   28   small_stool_carpet 16, log_bench_axe 6, log_bench_2_axe 6
  models/item/            121
  textures/block/          3   workbench_bot.png, workbench_top.png, workbench_top_sides.png
  textures/item/          18
  textures/gui/container/  1   workbench.png  ← ORPHAN, screen uses vanilla stonecutter.png
  lang/en_us.json          1   124 keys
  icon.png                 1
data/decor4fabric/
  recipes/               121   ← must become recipe/
  loot_tables/blocks/    120   ← must become loot_table/blocks/; workbench has NO loot table
  temporary recipes/      16   ← type "decor4fabric:workfence" is NEVER REGISTERED. Dead.
  tags/blocks/             5
data/minecraft/
  tags/blocks/            3   fences, fence_gates, wooden_fences
  tags/blocks/mineable/   1   axe.json
```

### 1.4 Bugs and oddities found while surveying

Fix these during the port. Most are one-liners, but they are all currently
*shipped behaviour* — so each one is a decision, not a cleanup, and items 7, 13
and 14 are cases where the fix is to make the accidental coupling explicit.

1. **`oak_acacia_log_chair.json` / `oak_acacia_log_chair_2.json` are misnamed,
   NOT duplicated.** There is no `oak_log_chair.json` in the tree; these two
   files are the *only* producers of `decor4fabric:oak_log_chair` and
   `decor4fabric:oak_log_chair_2`. Their contents are correct (`minecraft:oak_log`
   in, chair out) — only the filename has a stray `acacia` in it. **Rename**
   them to `oak_log_chair.json` / `oak_log_chair_2.json`; the ids are in the
   JSON, so this is a pure file rename. Verified 1:1: 32 chair recipes for 32
   chair blocks, each block having exactly one recipe. Deleting them — which is
   what a duplicate-detection pass suggests — makes both oak chairs
   unobtainable.
2. **16 dead recipe files** in `temporary recipes/` reference an unregistered
   serializer. The directory name contains a space, which is its own hazard.
3. **No loot table for `decor4fabric:workbench`** — the mod's own crafting
   station drops nothing.
4. **`itemRegistry` is empty and never called**; delete it.
5. **`textures/gui/container/workbench.png` is unused** — `workBenchScreen`
   deliberately reuses vanilla's `stonecutter.png`. Either use it or delete it.
6. **`Sit.INCORRECT_VERSION`** is a `static final Text` built at class-init, and
   its format string is `String.format("Please install Decor4Fabric" +
   mainDecor.getModVersion() + " for Minecraft x to play on this server.")` — no
   format specifiers, so it renders literally as
   `Please install Decor4Fabric1.2 for Minecraft x to play on this server.`
   (the `1.2` is `mod_version` from `gradle.properties` as a float).
7. **`SitEntity.OCCUPIED` is a public static `HashMap`** on a class that exists
   on both sides, and its two removal paths use **different key spaces**.
   `Sit` inserts with `new Vec3d(blockPos.getX(), blockPos.getY(),
   blockPos.getZ())` — integer block coords.
   `updatePassengerForDismount` removes with
   `new Vec3d(getBlockX(), getBlockY(), getBlockZ())` — *floored* entity coords —
   which **does** match, so a normal dismount works. But
   `SitEntity.remove(RemovalReason)` removes with `getPos()`, the exact entity
   position (`blockPos + 0.5`, `+0.17/+0.3/+0.35`), which can never match the
   insert key. So any removal that is *not* a passenger dismount — entity
   discarded, chunk unload, dimension change, `/kill` — **leaks the entry for the
   life of the server** and that seat is permanently "occupied".
   Separately, the map's *value* is `player.getBlockPos()` and is what
   `updatePassengerForDismount` returns, i.e. you are dismounted onto where your
   own feet already were; the seat's outward face is never computed.
8. **`getModVersion()` does `Float.parseFloat`** on the mod version. `1.19.2`,
   `1.20.1` and every `26.x` version fail this, and both sides fall through to
   `0.0f`, so it "works" — but any 3-part version that *does* parse asymmetrically
   will hard-disconnect players.
9. **`logBench`, `logBench2` and `logBench3` all call `sitMain()`** from
   `onUse` (lines 161 / 192 / 141), re-registering the `UseBlockCallback`
   handler on every sit. `mainDecor` already calls it exactly once, correctly.
   Handlers accumulate for the process lifetime.
10. **`storeAxe` has an unbounded generic** `<StateName>` and casts to `int`.
11. **License mismatch.** `LICENSE` and `README` say CC BY-NC-SA 4.0;
    `fabric.mod.json` says `cc-by-sa-4.0`. compress-em hit this exact thing and
    resolved it to the NonCommercial form. **You must decide**, because a
    multi-loader port means redistributing to Modrinth/CurseForge/NeoForge and
    the terms are not compatible with each other. This is a real decision, not
    a nit.
12. **8 wood types only** — no cherry, mangrove, bamboo, pale oak. **Resolved:
    cherry, mangrove and pale oak are added in this port** (§3.3.1), taking the
    registry from 121 to 166. Bamboo is excluded — it has no `bamboo_log` or
    stripped log, so it cannot fill this shape.
13. **Two independent `AXE_TYPE` properties that share a name.**
    `logBench.AXE_TYPE` and `logBench2.AXE_TYPE` are *both*
    `IntProperty.of("axe_type", 0, 6)`. `Sit` statically imports
    `logBench.AXE_TYPE` and calls `state.get(AXE_TYPE)` on every block in the
    `benches` tag — which contains the 8 `*_log_bench` **and** the 8
    `*_log_bench_2` ids, 16 in total. It only works because `BlockState.get`
    resolves by property *name* and the two constants happen to be named
    identically.
    `logBench3` (high bench) has no `axe_type` and is reached through
    `high_benches` instead. Unify to one constant so this stops being load-
    bearing luck.
14. **`logSmallStool`'s `wool_color` uses 0 as a sentinel — do not "fix" it.**
    `IntProperty.of("wool_color", 0, 16)`: **0 means "no carpet"**, and 1..16
    map to the 16 dye colours in dye order (1=white, 2=orange … 16=black). The
    blockstates carry entries for 1..16 only; `onUse` gates carpet
    placement/removal on `get(WOOL_COLOR).equals(0)`. This is correct today and
    looks like an off-by-one. (The `.equals(0)` is `Integer.equals(int)` and
    only works because the int autoboxes — prefer `== 0`.)

---

## 2. Target matrix and the "one jar per minor line" policy

### 2.1 Why these four

| MC | Why |
|---|---|
| **1.21.11** | Last **obfuscated** Java release. Keeps the Yarn/Loom-remap path alive and retains the largest Fabric audience. Also the cheapest of the four to get right, because most of the familiar 1.19/1.20 idioms still exist. |
| **26.1** | First unobfuscated Java release (March 2026). Java 25. **This is the real port** — everything below is "make 26.1 work, then copy it up". |
| **26.2** | Pure copy of 26.1 + a new `pack.mcmeta`. |
| **26.3** | Pure copy of 26.1 + a new `pack.mcmeta`. NeoForge is **still beta-only** here — do not publish as stable. |

**Do not target 1.19.x/1.20.x.** The gap from 1.18.2 is already four years; adding
four intermediate targets quadruples the work for versions nobody is on.

### 2.2 The policy (copy verbatim from compress-em `settings.gradle.kts`)

> One target per Minecraft **minor** line, never per patch. Patches inside a line
> share a pack format, so one jar serves them all. Minors do not. Never add a
> `26.1.2` or `26.2.1` target.

Concretely, `26.1` covers `26.1`, `26.1.1`, `26.1.2` (and any future `26.1.3`
with zero code changes — you only extend a hand-maintained publish list).

Pack formats to expect — **re-verify these against minecraft.wiki before you
commit them**, they move with every patch:

| Target | `min_format` | `max_format` |
|---|---|---|
| 1.21.11 | `[75,0]` | `[94,1]` |
| 26.1 | `[84,0]` | `[101,1]` |
| 26.2 | `[88,0]` | `[107,1]` |
| 26.3 | `[97,1]` | `[121,0]` |

### 2.3 Toolchain

Verified against the live registries on 2026-09-28:

| | |
|---|---|
| Gradle | **9.7.1** (Prism requires 9.7+) |
| Gradle daemon JDK | **25** |
| `dev.prism.settings` / `dev.prism` | **0.6.0** (pinned, not `+`) |
| `org.gradle.toolchains.foojay-resolver-convention` | 0.9.0 |
| Fabric Loom (bundled by Prism) | 1.18.2 |
| ModDevGradle (bundled) | 2.0.147 |
| Fabric Loader | 0.19.5 |
| NeoForge | 21.11.45 (1.21.11), 26.1.2.112 (26.1), 26.2.0.88 (26.2), 26.3.0.26-beta (26.3) |
| Mappings | **Mojang official** — the *absence* of a `yarn(...)` call is what selects it |
| Per-target Java | 21 for 1.21.11, 25 for 26.x (Prism auto-detects; don't hardcode) |

Fabric API, from `maven.fabricmc.net` (Prism resolves these; grab the newest
that exists for your MC line at the time you build): `0.141.6+1.21.11`,
`0.155.3+26.1.2`, `0.161.0+26.2`, `0.161.0+26.3`.

### 2.4 Three places must agree on the version range

This is compress-em's single most-repeated lesson and it fails *silently* in
one of the three places:

1. `build.gradle.kts` → `minecraftVersions("26.1", "26.1.1", "26.1.2")` — the
   **publish** list. Miss an entry here and users simply cannot find the
   release. Nothing errors.
2. `fabric.mod.json` → `"minecraft": ">=26.1 <26.2"`. Fabric rejects Maven
   range syntax; a bare `"26.1"` is EXACT, and a bare `">=26.1"` leaks into 26.2.
3. `neoforge.mods.toml` → `versionRange = "[26.1,26.2)"`, and
   `versionRange = "[26.1,)"` for the NeoForge dep.

**Do not write `${neoforge_version}` in the NeoForge dependency range.** Prism
substitutes the exact build you compiled against and turns it into a floor like
`[26.1.2.112,)`, which locks out 26.1.0/26.1.1 users whose NeoForge builds sort
*below* that. Write the bare minor line.

---

## 3. Phase 0 — Decisions (SETTLED 2026-09-28)

All four answered. Phase 0 is done; this section is now the record of what was
decided and what each answer obliges you to build.

| # | Decision | Answer | Consequence |
|---|---|---|---|
| 3.1 | version-lock disconnect | **Drop the login handshake; add a friendly play-phase ping** | §3.1.1. Deletes the mixin risk. Adds one custom payload + a small platform seam. |
| 3.2 | workbench recipe | **Option A — keep a real `decor4fabric:workbench` recipe type** | §3.2.1. Phase 4 stays in scope (~2–3 days). `CuttingRecipe` rewrite + codec-based recipe + `RecipeBookMenu` on the handler. |
| 3.3 | new wood types | **All three now: cherry, mangrove, pale oak** | §3.3.1. Registry grows 121 → **166** ids. The generator (§8) is now mandatory, not optional. |
| 3.4 | license | **CC BY-NC-SA 4.0** | §3.4.1. One metadata value: `CC-BY-NC-SA-4.0` everywhere. |

### 3.1.1 Ping instead of login lock

Delete `serverDecor`'s login check, `clientDecor`'s login networking, the
`decor4fabric:version_check` login channel, `Sit.INCORRECT_VERSION`, and
`mainDecor.getModVersion()`'s `Float.parseFloat`.

Replace with a play-phase version check:

- **One `CustomPacketPayload` named `decor4fabric:version_check`**, reusing the
  existing identifier. The payload type and its `StreamCodec` are vanilla
  (`CustomPacketPayload.Type` + `Registries.PLAY`), so they live in `common`.
- **Compare an integer, not the mod version string.** Add
  `public static final int PROTOCOL_VERSION = 1;` and bump it on any breaking
  content change. Do not parse version strings — that is what broke
  `Float.parseFloat` (§1.4 item 8), and string comparison is worse. The mod
  version itself is then free to be `2.0.0` without touching the protocol.
- Client sends the payload on join (Fabric: `ClientPlayConnectionEvents.JOIN`;
  NeoForge: a payload handler registered at `RegisterPayloadHandlersEvent`).
  Server compares, and on mismatch calls `player.disconnect(...)` with a
  `Text.translatable("decor4fabric.wrong_version", ...)`. Build that `Text`
  per-call, never in a `static` initializer.
- **This needs a platform seam** — correcting an earlier draft of this plan,
  the payload *definition* is cross-loader but the *send/receive registration*
  is not, on either loader. Add a `VersionCheckRegistrar` interface beside
  `ContentRegistrar` (§5.3), ~25 lines per loader. Still far cheaper than
  mixins into the login phase.
- Behaviour change to accept: the client now sees the world for a moment
  before being kicked, instead of being rejected pre-world. That is the price
  of not injecting into the login phase, and it is the right trade.

Keep the MC-line pin in `fabric.mod.json` / `neoforge.mods.toml` `depends`
blocks — that is what stops a 1.21.11 client from even attempting a 26.1 world.

### 3.2.1 Option A: the custom recipe type, specified

`workBenchRecipe extends CuttingRecipe`, and **`CuttingRecipe` no longer
exists** (added 1.19.3 for the mace, removed in 1.20). So this is a rewrite:

- `class WorkBenchRecipe implements Recipe<SingleRecipeInput>`.
  **Verify `SingleRecipeInput` exists on 1.21.11**; if the name is still
  `RecipeInput` there, that is a per-target import difference and must be named
  in the `diff -r` `--exclude` list (§10.1).
- Codec-based, via `RecordCodecBuilder` → `MapCodec`, replacing the current
  `read(Identifier, JsonObject)`. Fields: `Ingredient ingredient`,
  `ItemStack result` (which carries the count, so `1 log → 3 chairs` needs no
  separate field).
- `canCraftInDimensions(1, 1) → true`; `assemble` returns the stack; `matches`
  is `input.size() == 1 && ingredient.test(input.item())`.
- Register the `RecipeType` in **`Registries.RECIPE_TYPE`** from the
  `ContentRegistrar` seam (§5.3) so both loaders get it, deferred. Note this is
  now *one* vanilla registry entry — modern NeoForge does not need a separate
  recipe-type registration, unlike the 1.19-era advice this replaces.
- **`workBenchScreenHandler` must implement `RecipeBookMenu`.** This is the step
  that makes the workbench usable rather than merely openable: without
  `getRecipeBookEntries()` the recipes are reachable only by hand and the
  scrollable grid has no contents. Vanilla's `StonecutterScreenHandler` is the
  reference — port its recipe-book wiring alongside the handler.
- Recipe JSON must be **byte-identical across all four targets**, since the
  generator output is committed and shared. Emit the 1.20.5+ shapes
  (`Ingredient` as a list, `result` as `{"id": …, "count": …}`); all four
  targets parse those, so one format serves all.
- Phase 4 therefore stays: the GUI rewrite is unavoidable regardless, and the
  recipe rewrite rides along with it.

### 3.3.1 The three new woods, specified

45 new block ids, 15 per wood, on top of the existing 121:

```
<wood>_log_bench            <wood>_log_chair          <wood>_log_table
<wood>_log_bench_2          stripped_<wood>_log_chair       stripped_<wood>_log_table
<wood>_log_bench_3          <wood>_log_chair_2         <wood>_log_fence
<wood>_log_small_stool      stripped_<wood>_log_chair_2  stripped_<wood>_log_fence
stripped_<wood>_log_small_stool                            <wood>_log_fence_gate
                            stripped_<wood>_log_fence_gate
```

for `wood ∈ { cherry, mangrove, pale_oak }` → **121 + 45 = 166 blocks**.

- **Naming rule:** all three use the `_log_` form
  (`cherry_log_bench`, `mangrove_log_bench`, `pale_oak_log_bench`). Note the
  existing table is *not* uniform — the six overworld woods use `_log_` and
  crimson/warped use `_stem_` (`crimson_stem_bench`). Preserve that asymmetry
  for the existing ids; do not normalise it (§5.1).
- **Bamboo is deliberately excluded.** There is no `bamboo_log` and no stripped
  bamboo log, so it cannot fill this shape. (Bamboo *blocks* are a different
  item with different models — out of scope.)
- All three exist on 1.21.11: cherry and mangrove since 1.19.4, pale oak since
  1.21.4. Nothing is 26.x-gated, so no target-specific content.
- The 45 blocks are **appended after** the 121 existing ids. Appending is safe:
  namespaced ids are not order-dependent. Tier 1 (the 121) is still the
  compatibility contract.
- Budget: +45 blocks × (blockstate, block model, item model, loot table,
  recipe, lang key) ≈ **270 new JSON files**, on top of the 815. Do not
  hand-author any of them — this is what makes §8's generator a hard
  requirement rather than a nicety.
- Tag growth (§9): `benches` 16 → 22, `high_benches` 8 → 11, `small_stools`
  16 → 22, `chairs` 32 → 44, `tables` 16 → 22. Plus 6 new fences and 6 new
  fence gates into `minecraft:fences` / `minecraft:fence_gates`.

### 3.3.2 Does this make the port bigger than it should be?

Worth being explicit: +45 blocks is the *only* part of this plan whose cost
scales with content rather than with code, and it lands on the generator rather
than on hand-written Java. The four Java classes that carry the mod's behaviour
(`logBench`, `logBench2`, `logSmallStool`, `logChair`) are shared by all 11
woods and are untouched by the addition. If the generator in Phase 5 is late,
the new woods are the part to cut — drop them to a follow-up release and
nothing else has to change.

### 3.4.1 Applying the license

One decision, five files. The SPDX id is `CC-BY-NC-SA-4.0`; keep that exact
string in metadata so Modrinth and CurseForge parse it, and keep the SPDX id
out of the human-readable `LICENSE`/`README` text (where it should read
"CC BY-NC-SA 4.0" with spaces).

- `LICENSE` — already CC BY-NC-SA 4.0. **No change.**
- `README.md` — already CC BY-NC-SA 4.0. **No change.**
- Prism `build.gradle.kts` `metadata.license` — set to `CC-BY-NC-SA-4.0`
  (§4.2, already written that way).
- Every `fabric.mod.json` — the **only file that is currently wrong**:
  `cc-by-sa-4.0` → `cc-by-nc-sa-4.0`.
- Every `neoforge.mods.toml` — `${license}` expands to the Prism value, and it
  sits at the file **root**, not inside `[[mods]]` (§12 trap 3).

One consequence worth stating plainly, since it is the reason this was worth
asking about: NC means nobody may use the mod commercially, including inside a
paid modpack. That is compatible with Modrinth/CurseForge/NeoForge
redistribution, so nothing about the port is blocked — but it is a constraint
on *users*, and it is now stated consistently, so nobody discovers it by
reading two files that disagree.

---

## 4. Phase 1 — Project skeleton (Prism)

~1 day. Do this on a `port/26.x` branch. Leave `main` frozen at 1.18.2 forever
so old worlds/datapacks keep a home, and tag it `v1.2-1.18.2` first.

Delete `build.gradle`, `settings.gradle`, `gradle.properties`, and the whole of
`src/`. Add:

```
build.gradle.kts          ← the ONLY build script
settings.gradle.kts       ← the ONLY settings script
gradle.properties         ← JVM args only
versions/1.21.11/{common,fabric,neoforge}/
versions/26.1/{common,fabric,neoforge}/
versions/26.2/{common,fabric,neoforge}/
versions/26.3/{common,fabric,neoforge}/
common/README.md          ← documents that it is Minecraft-free and currently empty
```

There are **no `build.gradle.kts` files under `versions/`**. Prism configures
every subproject from the root. Adding one will be evaluated by Gradle and can
conflict with Prism's own configuration.

### 4.1 `settings.gradle.kts` skeleton

```kotlin
pluginManagement {
    repositories {
        maven { url = uri("https://maven.leclowndu93150.dev/releases") }  // Prism FIRST
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://maven.fabricmc.net/") }
        maven { url = uri("https://maven.neoforged.net/releases") }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
    id("dev.prism.settings") version "0.6.0"   // pinned, not "+"
}

rootProject.name = "decor4fabric"

prism {
    sharedCommon()   // root common/, must not touch MC classes
    version("1.21.11") { common(); fabric(); neoforge() }
    version("26.1")    { common(); fabric(); neoforge() }
    version("26.2")    { common(); fabric(); neoforge() }
    version("26.3")    { common(); fabric(); neoforge() }
}
```

### 4.2 `build.gradle.kts` skeleton

```kotlin
plugins { id("dev.prism") }

group = "net.gmsgarcia.decor4fabric"
version = "2.0.0"     // breaking: new target + loader matrix

prism {
    metadata {
        modId = "decor4fabric"
        name = "Decor4Fabric"
        description = "A decoration mod with furniture blocks that mostly have special features."
        license = "CC-BY-NC-SA-4.0"   // decision 3.4 — matches LICENSE + README
        author("GmsGarcia#1553")
        archivesName = "decor4fabric-{mc}-{loader}"
        expand("homepage", "http://gmsgarcia.ga/decor")
        expand("sources", "https://github.com/GmsGarcia/decor4fabric")
    }

    // No yarn() anywhere: its absence selects Mojmap.

    version("1.21.11") {
        accessWidener("versions/1.21.11/common/src/main/resources/decor4fabric.accesswidener")
        javaVersion = 21
        minecraftVersions("1.21.11")
        fabric { loaderVersion = "0.19.5"; fabricApi("0.141.6+1.21.11") }
        neoforge { loaderVersion = "21.11.45" }
    }

    version("26.1") {
        accessWidener("versions/26.1/common/src/main/resources/decor4fabric.classtweaker")
        javaVersion = 25
        minecraftVersions("26.1", "26.1.1", "26.1.2")
        fabric { loaderVersion = "0.19.5"; fabricApi("0.155.3+26.1.2") }
        neoforge { loaderVersion = "26.1.2.112" }
    }
    // 26.2 and 26.3: same shape, own pack.mcmeta.
}
```

### 4.3 Acceptance criteria for Phase 1

Verified on 2026-09-28. All eight boxes are ticked; the corrections below were
found by running them rather than by reading them.

- [x] `./gradlew prismDoctor` runs clean on all 8 loader projects and reports
      `mappings: ...` and the right underlying plugin for each. It reports
      `fabric-loom-remap` (named dev / intermediary production) for 1.21.11
      Fabric, plain `fabric-loom` (unobfuscated) for the three 26.x Fabric
      targets, and `net.neoforged.moddev` (neoform named dev) for all four
      NeoForge targets. No `yarn` anywhere, so Mojmap.
- [x] `./gradlew :26.1:fabric:build` produces
      `decor4fabric-26.1-Fabric-2.0.0.jar` with no sources.
- [x] `./gradlew :26.1:neoforge:build` likewise.
- [x] `pack.mcmeta` exists in **every** `versions/*/common/src/main/resources/`.
      **CORRECTION to the original wording:** it does *not* need to be in
      `versions/*/{fabric,neoforge}/src/main/resources/`. Prism only fails a
      subproject that has `assets/` or `data/` without one, and the loader
      resource roots hold only `fabric.mod.json` / `neoforge.mods.toml`. The
      `common` `pack.mcmeta` is the one that reaches the jar.
- [x] `./gradlew :26.1:fabric:runClient` and `:26.1:neoforge:runClient` both
      load the mod. Fabric logs `Decor4Fabric loaded` and
      `Decor4Fabric client ready`; NeoForge logs the mod in its mod list, then
      `Decor4Fabric loaded`, with **zero** ERROR/FATAL lines. Both reach resource
      reload, after which the title screen renders. The only Fabric errors are
      Mojang `InvalidCredentialsException: Status: 401` from the unauthenticated
      dev account, which are unrelated to the mod.
- [x] **Never run `./gradlew clean build` as one command.** Confirmed, not
      assumed: the generated transformers really do land in
      `versions/26.{1,2,3}/neoforge/build/tmp/neoformruntime/*/ats/accesstransformer.cfg`
      at configuration time. Documented in the README.
- [x] All `${...}` in `fabric.mod.json` and `neoforge.mods.toml` expand to the
      intended values in the packaged jars.
- [ ] **NeoForge access transformer actually carries a mod entry.** The
      transformers currently generated are NeoForge's own, because the two
      access-control files are header-only until Phase 2 adds
      `CreativeModeTab$Output`. Add that entry and confirm it appears in the
      generated `.cfg`. Note 1.21.11 NeoForge did not produce a
      `neoformruntime` transformer at all, so that target needs its own check.
- [ ] NeoForge client setup (`FMLClientSetupEvent`) is wired in Phase 2 alongside
      the real client content. Phase 1 has a Fabric client entrypoint but no
      NeoForge counterpart, because `ClientModInitializer` is stable enough to
      verify the entrypoint slot while the NeoForge equivalent would have been
      speculative with no content to attach it to.

#### Two bugs this phase caught that reading the plan did not

1. **Loader-specific range syntax.** Fabric rejects Maven range syntax and wants
   `>=26.1 <26.2`; FML wants `[26.1,26.2)`. A single shared placeholder put the
   Fabric form in `neoforge.mods.toml`, which compiles fine and then fails at
   load. `neoforge.mods.toml` now carries a separate `__NF_MC_RANGE__`.
2. **Prism expands `${...}` inside TOML comments too**, so a comment explaining
   the NeoForge floor came out reading `Deliberately NOT "26.1.2.112"`. Comments
   must avoid `${}` or be worded so the expansion still reads correctly.

`settings.gradle.kts` also needs
`maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }`.
Prism puts `net.minecraftforge.gradle:ForgeGradle` on the settings classpath and
that artifact is only reachable through Sponge's mirror; without it the build
dies at configuration time before any target is set up.

### 4.4 Access control

One declaration, two loaders, two file formats. Prism's `accessWidener(path)` is
the **only** place you declare it; it feeds Fabric as-is and converts to an
access transformer for NeoForge.

`versions/1.21.11/common/src/main/resources/decor4fabric.accesswidener`
```
accessWidener	v2	named
```
`versions/26.1/common/src/main/resources/decor4fabric.classtweaker` (identical
for 26.2/26.3)
```
classTweaker	v1	official
```

Two header lines are the *entire* 1.21.11 → 26.x access-control difference:
obfuscated target needs `named`; unobfuscated 26.x needs `official` (which on
26.x **is** the mojmap name).

What you will likely need to widen — add entries as the compiler asks, and
write a comment naming the exact error for each:

| Declaration | Why |
|---|---|
| `accessible class net/minecraft/world/item/CreativeModeTab$Output` | 26.1 made it `protected`, so `displayItems` cannot be implemented at all. You have 4 creative tabs. |
| `accessible class net/minecraft/world/inventory/Inventory` (or the `Container` helper) | if the `impl_Inventory` interface shape no longer lines up |
| `accessible method net/minecraft/world/item/BlockItem <init> ...` | only if you need to override the description id (you shouldn't — use `useBlockDescriptionPrefix()`) |
| fence/fence-gate shape methods | see §5.5 — if subclassing vanilla `FenceBlock` stops working |

### 4.5 Metadata templates

`versions/26.1/fabric/src/main/resources/fabric.mod.json`:
```json
{
  "schemaVersion": 1,
  "id": "${mod_id}",
  "version": "${version}",
  "name": "${mod_name}",
  "description": "${description}",
  "authors": ["${mod_author}"],
  "contact": { "homepage": "${homepage}", "sources": "${sources}" },
  "license": "${license}",
  "icon": "assets/decor4fabric/icon.png",
  "environment": "*",
  "entrypoints": {
    "main": ["net.gmsgarcia.decor4fabric.fabric.FabricDecor4Fabric"],
    "client": ["net.gmsgarcia.decor4fabric.fabric.client.FabricDecor4FabricClient"]
  },
  "depends": {
    "fabricloader": ">=${fabric_loader_version}",
    "fabric-api": "*",
    "minecraft": ">=${minecraft_version} <26.2",
    "java": ">=${java_version}"
  }
}
```

Note: **no `mixins` array** — decision 3.1 removed the only thing that would
have needed one — and **no `server` entrypoint**, since the version handshake is
gone (§3.1.1); the ping registers from the common init. Prism expands `${...}` in
`fabric.mod.json`,
`mods.toml`, `neoforge.mods.toml`, `pack.mcmeta` and `*.mixins.json`. JSON files
get their newlines escaped, so keep them single-line-compact.

`versions/26.1/neoforge/src/main/resources/META-INF/neoforge.mods.toml`:
```toml
modLoader = "javafml"
loaderVersion = "${neoforge_loader_version_range}"
# FML reads these from the ROOT of the file, not from [[mods]].
license = "${license}"
logoFile  = "assets/decor4fabric/icon.png"   # 26.2+ renamed this to iconFile

[[mods]]
modId = "${mod_id}"
version = "${version}"
displayName = "${mod_name}"
description = "${description}"
authors = "${mod_author}"

[[dependencies.decor4fabric]]
modId = "neoforge"
type = "required"
versionRange = "[26.1,)"
ordering = "NONE"
side = "BOTH"

[[dependencies.decor4fabric]]
modId = "minecraft"
type = "required"
versionRange = "[${minecraft_version},26.2)"
ordering = "NONE"
side = "BOTH"
```

`logoFile` → `iconFile` is a genuine loader break at 26.2: NeoForge deprecated
`logoFile` and warns at startup. Keep `logoFile` for 1.21.11 and 26.1.

Also in this phase: rewrite `.github/workflows/build.yml` (see §10.2). The
current one uses `actions/*@v1`/`@v2` and `ubuntu-20.04`, both long dead.

---

## 5. Phase 2 — Content refactor (the biggest structural change)

~2 days. Do this **before** touching any API that changed, because it makes the
API migration a mechanical loop over ~10 classes instead of 595 lines of
hand-written `Registry.register`. The one exception is §7, the sit redesign,
which has to land first because it introduces the occupancy blockstate this
table must be able to express.

The target shape is compress-em's, verbatim:

| compress-em | decor4fabric |
|---|---|
| `content/BlockSpec.java` — one record, the only place a block is built | same |
| `content/BlockFamilies.java` — shared specs | same, for `logBench`, `logBench2`, `logBench3`, `logSmallStool`, `logChair`, `logTable`, `logFence`, `logFenceGate`, `workBench` |
| `content/CompressBlocks.java` — the 130-entry table | `content/DecorBlocks.java` — the 166-entry table (121 + 45, §5.1) |
| `VanillaRegistrar` / `ContentRegistrar` / `Once<T>` | same |

### 5.1 Why the table has to be the compatibility contract

121 existing worlds contain 121 block ids. Those ids are the only thing that
survives a port. compress-em wrote this into the file itself:

> This list is the compatibility contract. Ids and their order carry over from
> 1.17 unchanged, so worlds, saves and datapacks built against that version
> still resolve. Treat any edit to it as a deliberate change rather than a
> refactor.

Copy the ids out of `blockRegistry.java` mechanically. Do not "tidy" them.

**The table has two tiers, and they have different rules:**

- **Tier 1 — the 121 existing ids: frozen.** Same names, same relative order,
  no exceptions. An edit here is a breaking change.
- **Tier 2 — the 45 new ids from cherry/mangrove/pale oak (§3.3.1): appended
  after Tier 1.** Adding namespaced ids is not a breaking change, so these are
  free to add; but they go *after*, never interleaved, so a diff of the table
  shows additions only.

The exact 121 Tier-1 ids, by family. Note the two naming conventions — the six
overworld woods use `_log_`, crimson and warped use `_stem_`, and that
asymmetry is part of the contract:

```
workbench                                                          (  1)
oak|birch|spruce|dark_oak|acacia|jungle_log_bench                   (  6)
crimson_stem_bench, warped_stem_bench                              (  2)
  ..._bench_2                                                            ( 8)
  ..._bench_3                                                            ( 8)
  ..._small_stool                                                        ( 8)
  stripped_..._small_stool                                               ( 8)
  ..._chair, ..._chair_2                                                 (16)
  stripped_..._chair, stripped_..._chair_2                              (16)
  ..._table, stripped_..._table                                          (16)
  ..._fence, stripped_..._fence                                          (16)
  ..._fence_gate, stripped_..._fence_gate                              (16)
```

Tier 2 appends 15 ids per new wood for `cherry`, `mangrove`, `pale_oak`
(§3.3.1) → **166 total**.

Verify Tier 1's total against `assets/decor4fabric/lang/en_us.json` (124 keys
today = 121 blocks + 3 `itemGroup` keys) and against the 121 blockstates. They
should agree. After the port the invariant to assert is: **166 blocks = 166
blockstates = 166 item models = 166 loot tables = 166 recipes, and 169 lang keys
(166 + 3)** — see the generator assertion in §8.2.

### 5.2 `BlockSpec`

One record. The only place a `Block` is constructed. Includes the
`Properties.setId(key)` call, which is **mandatory** on 1.21.2+: drops became
data-driven and `BlockBehaviour$Properties.effectiveDrops()` throws
`NullPointerException: Block id not set` if the properties don't know their own
registry key. Build the `ResourceKey` once, use it for both the properties and
the registration.

### 5.3 The registration seam

Copy compress-em's `ContentRegistrar` interface as-is. The critical design
points, each of which was a real bug found during the compress-em port:

**Take `Supplier<T>`, not `T`.** NeoForge freezes all registries *before* it
constructs the `@Mod` class, so you cannot even build a `Block` there — its
constructor claims an intrusive registry holder and `MappedRegistry.validateWrite`
throws `Registry is already frozen`. Deferring only the `Registry.register` call
is not enough. Deferring construction is what makes one common bootstrap serve
both loaders.

**Put `VanillaRegistrar` in `common`, not `fabric/`.** It uses only
`BuiltInRegistries` + `Registry`, both vanilla, so it names no loader class and
is genuinely loader-neutral. Consequence: the Fabric subproject ends up with a
single ~30-line entrypoint file and has no counterpart to the NeoForge registrar.
A future third loader needs zero Fabric-side work.

**Keep two layers of bookkeeping.** `List<String> BLOCK_IDS` fills
synchronously at `init()`; `Map<String, Block> BLOCKS` fills lazily when the
factory runs (on NeoForge, during `RegisterEvent`). Gates and log lines read the
lists; block items and creative tabs read the maps. compress-em's original
`if (BLOCKS.isEmpty())` double-init guard became **vacuously false** under
deferred registration and its startup log printed zeros. This trap is silent.

**Memoize the factories** with a tiny `Once<T>`, so a factory invoked twice
returns the same instance. This is what guarantees the instance in your map is
the same one the registry holds.

**Enforce the order blocks → block items → creative tabs** by *attachment order*
of the three `DeferredRegister`s, not by hoping.

### 5.4 Creative tabs

Four tabs: `decor4fabric:seats`, `decor4fabric:tables`, `decor4fabric:fences`, and
`ItemGroup.DECORATIONS` (workbench only). Keep the three custom ids — datapacks
may reference them.

`FabricItemGroupBuilder` is deprecated/removed. Use vanilla
`CreativeModeTab.Builder` registered into `BuiltInRegistries.CREATIVE_MODE_TAB`
from the `ContentRegistrar`, exactly as compress-em's two tabs are built. This is
where the `CreativeModeTab$Output` access widener gets used.

Every `BlockItem` must be built with
`new Item.Properties().setId(key).useBlockDescriptionPrefix()`.
`Item` resolves its translation key once in its constructor from
`effectiveDescriptionId()` and `getDescriptionId()` is `final`, so a `BlockItem`
**cannot** fix it afterwards. Get this wrong and every item in the mod shows as
`decor4fabric.oak_log_bench` instead of "Oak Log Bench". compress-em hit this.

### 5.5 Block classes

Ten classes, seven of which are trivial. The migrations, roughly in order of
difficulty:

**`logFence` / `logFenceGate` (trivial subclasses).** `Material.WOOD` and
`Material.METAL` were removed; `FabricBlockSettings.of(Material)` is gone. Becomes:

```java
super(BlockBehaviour.Properties.of()
        .mapColor(MapColor.WOOD)
        .sound(SoundType.WOOD)
        .strength(2.0f, 3.0f)
        .setId(key));   // needs the key now
```

`FenceBlock`/`FenceGateBlock` themselves still exist; subclassing should still
work. That sample is for the **wood** blocks; `workBench` is the one
`Material.METAL` block and must keep a non-wood `mapColor` (e.g.
`MapColor.COLOR_GRAY`) and `SoundType.METAL` or its look and sound change
silently. The blockstates, though, use **string** `when` values
(`"north": "true"`, `"axe_type": 1`). Vanilla writes these as bare booleans
(`"north": true`) and numbers (`"axe_type": 1`); the string form is
legacy-compat and should be normalised by the generator in §8. Fence *and fence
gate* models must also be re-verified visually — the vanilla connection shape
logic has been touched repeatedly.

**`logTable` (trivial).** Same properties migration. It has a `LOG_TABLE` "rotated
90°" convention; confirm the blockstate `y` values are right.

**`workBench` (medium).** The hand-rolled waterlogging is obsolete:

- `tryFillWithFluid` and the `getStateForNeighborUpdate` manual
  `createAndScheduleFluidTick` loop were removed. Use vanilla's
  `SimpleWaterloggedBlock`.
- `getOutlineShape` is now `getShape(state, level, pos, context)` with a
  different parameter order, and the static `VoxelShape` built with
  `Block.createCuboidShape` moves. The 9-part union is still valid logic —
  it's the signature that changed.
- `getPlacementState` needs `@Override` now (it was an accidental override).
- `Block.NOTIFY_ALL` → `Block.UPDATE_ALL`.
- `NamedScreenHandlerFactory` is fine but `createMenu` gets
  `player.getLevel()` in newer versions.

**`logBench` / `logBench2` / `logBench3` / `logSmallStool` (hard — the real
work).** These four carry all the mod's behaviour:

1. **`AXE_TYPE` `IntProperty.of("axe_type", 0, 6)`** — keep, it's load-bearing
   for the blockstate model selection. **Unify it**: `logBench` and `logBench2`
   each declare their own identically-*named* constant, and `Sit` resolves one
   of them against both block families purely by name (§1.4 item 13). One shared
   constant in `BlockFamilies` retires that coincidence. `logBench3` has no
   `axe_type` — don't add one.
2. **`onBreak` is gone.** The
   `spawnBreakParticles(world, player, pos, state)` +
   `emitGameEvent((Entity) player, GameEvent.BLOCK_DESTROY, pos)` pair was
   replaced by vanilla doing it automatically in the destroy path. **Delete the
   override entirely** and verify particles and the breaking sound still play.
3. **Block-entity contents on break.** `onStateReplaced` with
   `ItemScatterer.spawn(world, pos, be)` is deprecated. The replacement is
   `onRemove(Block, BlockState, BlockState, BlockEntity, ItemStack)` plus
   `Containers.dropContents(level, pos, inventory)`. Getting this wrong loses
   stored axes on every break — a data-loss bug, so test it explicitly.
4. **`getComparatorOutput`** still exists but `ScreenHandler.calculateComparatorOutput`
   changed shape (`InventoryHolder` overloads). Check.
5. **`storeAxe`'s `<StateName>` generic** — just make it `int`. This was always
   an int.
6. **Drop the `sitMain()` call from `onUse`.** It re-registers the
   `UseBlockCallback` handler on every sit. Move sit handling into
   `Sit`/`SitEntity` or into a proper `InteractionUtil`.
7. `hasArmRests` as a ctor `boolean` → an enum or a blockstate property so the
   table in §5.1 can express it without a special case.

**Block entity NBT.** `readNbt`/`writeNbt` were renamed
`readAdditionalSaveData`/`writeAdditionalSaveData` (yarn: `readNbt` stayed but
the *signature* changed), and 1.19.3+ added `toInitialChunkDataNbt` and moved the
position out of the BE's own NBT. Concretely you now need:

- `loadAdditional(CompoundTag, HolderLookup.Provider)` /
  `saveAdditional(CompoundTag, HolderLookup.Provider)` (or the
  `CustomPacketPayload`-era equivalents, depending on target),
- `toInitialChunkDataNbt(RegistryAccess)` returning only the *contents*, not
  x/y/z,
- a `getUpdateTag(...)` for client sync.

`DefaultedList` was renamed `NonNullList` in 1.19.4. The `impl_Inventory` default-method
interface still works but `Inventories.readNbt`/`writeNbt` were replaced by
`Inventories.loadAllItems`/`saveAllItems` with a `HolderLookup.Provider`
argument.

**`BlockEntityType` registration.** `FabricBlockEntityTypeBuilder...build(null)`
passes a `null` `ModContainer` — that is an internal-impl signature and is gone.
Register the type with vanilla:
`BlockEntityType.Builder.of(supplier, blocks...).build(null)` is the vanilla
equivalent and `null` is still legal there, or use the Fabric API's public
`FabricBlockEntityTypeBuilder` if it still exists on your target. Add the block
entities to the `ContentRegistrar` seam (deferred construction) so NeoForge can
order them after the blocks.

---

## 6. Phase 4 — The workbench GUI

Numbered 4 but documented here first for readability; §7 is Phase 3 and runs
first — see §13.

~2–3 days. **In scope** — decision 3.2 chose option A, so the custom recipe type
stays and this phase carries both the recipe rewrite (§3.2.1) and the GUI
rewrite. Build the recipe first: the recipe-book wiring in the handler is the
part with real logic, and it is what makes the workbench usable.

`workBenchScreen.java` is a fork of `StonecutterScreen`. Every single line of its
render path is obsolete:

| 1.18.2 (current) | Now |
|---|---|
| `MatrixStack` | `GuiGraphics` |
| `drawTexture(matrices, x, y, u, v, w, h)` | `blit(...)` |
| `renderInGuiWithOverrides(stack, x, y)` | `renderGuiItem(...)`; 1.20.5+ takes an `int` z |
| `RenderSystem.setShader(...)` | **gone entirely** in 1.20.5 |
| `PositionedSoundInstance.master(...)` | `SimpleSoundInstance.forUI(...)` |
| `renderTooltip` / `drawMouseoverTooltip` / `renderBackground` | all reshaped |
| hard-coded geometry constants | unchanged (good) |
| `TEXTURE = minecraft:textures/gui/container/stonecutter.png` | unchanged — you're still borrowing the stonecutter GUI |

Recommendation: **rewrite it, don't port it.** It's ~160 lines of a vanilla file
you cannot see the current version of. Plan:

1. Write `WorkBenchRecipe` first — codec-based
   `Recipe<SingleRecipeInput>` with a `MapCodec` — and register the `RecipeType`
   in `Registries.RECIPE_TYPE` through the `ContentRegistrar` seam. Full spec in
   §3.2.1. Do this before the handler, so you have real recipes to list.
2. Port `workBenchScreenHandler` next, and have it **implement `RecipeBookMenu`**.
   This is the step most often missed: without `getRecipeBookEntries()` the
   workbench opens empty and the recipes can only be crafted by hand. Also carries
   the real logic — the 4×3 scrollable recipe grid, `selectedRecipe` property,
   `scrollAmount`/`scrollOffset`, the per-world-tick sound debounce.
3. Then the screen: blit the stonecutter background, then recipe-book entry
   rendering, then the scrollbar (12×15 px, 4 columns × 3 rows,
   `SCROLLBAR_AREA_HEIGHT = 54`, offset 52/14), then tooltips.
4. `ScreenHandler.onButtonClick` → in newer versions the button id plumbing
   changed. Check the current vanilla `StonecutterScreenHandler` for the
   property-click path.

The recipe JSON this phase consumes is generated in Phase 5 (§8), so between
Phases 4 and 5 the 121 existing recipe files need hand-migrating to the new
shape — or do Phase 5's generator for recipes only, early, and fold the rest in
later. Do not leave the old `decor4fabric:workbench` JSON in place past Phase 5;
a stale `read(Identifier, JsonObject)`-era file is a datapack error, not a
warning.

---

## 7. Phase 3 — Sit system

Numbered 3 because it must land before the content table, which has to be able
to express the occupancy blockstate it introduces — see §13.

~1 day. This is a genuine redesign, not an API migration.

`SitEntity` is a 0.001×0.001 invisible entity that the player rides, and
`SitEntity.OCCUPIED` is a **public static `HashMap<Vec3d, BlockPos>`**.

Problems that must be fixed regardless of MC version:

1. **The map is not persisted and not server-authoritative.** It's a static on a
   class loaded on both sides. A `HashMap` keyed by `Vec3d` doubles does not
   survive a chunk unload, a dimension change, or a server restart. Two players
   on different chunks of the same dimension share it in one JVM.
2. **The keys don't match** (§1.4 item 7): `Sit` inserts at integer block
   coords, `updatePassengerForDismount` deletes at floored entity coords (that
   one works), and `remove()` deletes at the exact entity position (that one
   never matches, so the entry leaks).
3. **`SitEntity.remove()` is only called on the entity's own path.** If the chunk
   unloads with a passenger, the entry leaks.
4. **It is a network-visible entity.** A custom `EntityType` needs a real spawn
   packet; `createSpawnPacket()` returning `new EntitySpawnS2CPacket(this)` is
   the old API and the entity-tracker path has been reworked.
5. `EntityRenderer.getTexture` is no longer part of `EntityRenderer`;
   `getRenderOffset` and `getBlockLightAmount` are now required. The current
   `EmptyRenderer` returning `null` for a texture was already a lie.

Design to replace it with:

- **Occupancy as block state.** Each seatable block already has blockstates.
  Add `OCCUPIED` (an `EnumProperty<Yes/No>` or a `BooleanProperty`) to chairs,
  benches, and stools, and drive the sit/dismount from state transitions. This is
  persisted for free, is visible in F3, and syncs to the client. It is also what
  most shipped sit mods converged on.
- **Seat position as a blockstate property or a per-block override.** The four
  heights (0.17 / 0.3 / 0.35) currently live in `Sit`'s if-chain. Move them into
  the block classes.
- **`SitEntity` becomes a mount-only marker**, or is removed entirely in favour
  of directly setting the player's position and rotation. The marker exists only
  because `startRiding` gives you a clean dismount. If you keep it: register
  `EntityType` with the modern builder, implement the required renderer methods
  (returning `false` from `shouldRender`), and make `remove` idempotent.

Sneaking-vs-item-in-hand gating (`sneakingAndEmpty`) and
`world.canPlayerModifyAt(player, pos)` should be preserved as-is — that logic is
correct, it's just currently unreachable because it sits behind a callback that
re-registers itself.

---

## 8. Phase 5 — Resources: generate, don't hand-maintain

~1.5 days. 815 files is the single largest maintenance liability in the mod, and
it is entirely mechanical.

### 8.1 Don't use loader datagen

Tempting (`fabric { datagen() }`, automatic on NeoForge), but: Fabric writes to
`src/main/generated`, NeoForge writes to `src/generated/resources`, and on
1.21.4+ NeoForge splits into `clientData`/`serverData`. You have four version
trees × two loaders. You would be generating the same JSON eight times and
differing.

**Instead: a plain `main()` in `common` that writes into a checked-in
`src/main/generated/resources`, run once, commit the result.** Prism's per-target
`common` has full vanilla Minecraft on its compile classpath, so it can read
`Registries.BLOCK` and friends. This is what compress-em effectively did
(the CHANGELOG references a generator; the JSON is checked in, perfectly
parallel, which is consistent with generated-then-committed).

### 8.2 What the generator emits, and the format changes each needs

Counts are **post-port**: 166 blocks, i.e. the 121 existing plus 45 from
cherry/mangrove/pale oak (§3.3.1). This is now mandatory work, not an
optimisation — hand-authoring 270 extra JSON files is not viable.

The generator should be **parameterised over (wood × shape)** rather than
iterating a flat id list. That is the whole reason the three new woods are cheap:
`for (wood : WOODS) for (shape : SHAPES)` produces all 166 with one code path, and
the 19 abstract parent models are reused as-is.

| Asset | Count | Changes required |
|---|---|---|
| `assets/.../blockstates/*.json` | 166 | `recipes/`→`recipe/` sibling; **`"north": "true"` → `"north": true`**; keep multipart; bench states combine `facing` × `axe_type` (28 parts each) |
| `assets/.../models/block/*.json` | 167 | mostly unchanged; 19 abstract `models/` parents stay hand-written (they're `elements` + `display`, not generated data) |
| `assets/.../models/item/*.json` → `assets/.../items/*.json` | 166 | **1.21.9+ replaced `models/item/` with `items/`** containing `{"model": {"type": "minecraft:model", "model": "decor4fabric:block/…"}}` |
| `data/.../loot_tables/blocks/` → `loot_table/blocks/` | 166 | directory rename **and** add the missing `workbench` table (120 → 166 is 121 + the 45 new); add `"random_sequence"` |
| `data/.../recipes/` → `data/.../recipe/` | 166 | directory rename; `Ingredient` object→array; `"result": "id"` → `"result": {"id": …, "count": n}`; keep `"type": "decor4fabric:workbench"` (§3.2.1) and emit the identical shape on all four targets; **delete the 16 dead `temporary recipes/`** and **rename** the 2 misnamed `oak_acacia_log_chair*` (§1.4 item 1) |
| `data/minecraft/tags/blocks/*` → `tags/block/*` | 4 | directory rename (`blocks`→`block`); `minecraft:wooden_fences` is **removed** — you must be in `minecraft:fences`; `"replace": false` + flat `"values": []`; ship `mineable/axe.json` and add all 22 fences and 22 fence gates |
| `lang/en_us.json` | 1 | 124 keys → **169** (166 blocks + 3 `itemGroup`); add `itemGroup.decor4fabric.*` if the tab ids change |

**One cross-check worth automating in the generator:** emit
`workbench` + every block id, and assert the set equals
`BuiltInRegistries.BLOCK`'s `decor4fabric:*` keys. That single assertion catches
an unregistered block, a stale file, and a misnamed recipe in one go — it is the
cheap test that makes 166 blocks tractable.

The `axe_type` property and the fence `north/east/south/west` properties are the
two places where a generator pays off most: the bench blockstates are 28
multipart entries per wood type, hand-typed, and **already internally
inconsistent**. In `oak_log_bench.json` the six `axe_type: 1..6` groups use
`y = 0/90/180/270` correctly for north/east/south/west — but the `axe_type: 0`
(no axe) group is wrong for two of the four sides: **south has no `"y"` at all**
(should be 180) and **west has `"y": 90`** (should be 270). So a bench with no
axe stuck in it renders facing the same way on two of four sides, in the same
file where the axe variants are correct. The fence blockstates *are* correct
(0/90/180/270). **Verify every rotation visually after generating** — this is
exactly where hand-authored blockstates go wrong, and the generator is what will
stop it recurring across 11 woods.

### 8.3 Sounds and models that are not data

The `workbench.png` GUI texture and the 18 `textures/item/*` are hand-authored;
leave them — the 45 new blocks need no new art, because every model reuses a
vanilla `<wood>_planks` / `<wood>_log` texture. Delete
`textures/gui/container/workbench.png` or wire it in; the ported screen keeps
borrowing vanilla's `stonecutter.png` either way (§3.2.1 keeps the GUI, not
necessarily this file).

---

## 9. Phase 6 — Fuel, tags, and the remaining stragglers

~0.5 day.

- **`fuelRegistry` imports `net.fabricmc.fabric.impl.content.registry.FuelRegistryImpl`.**
  That is Fabric API *internals* and has no cross-loader story. Pick one:
  - platform seam (a `FuelRegistrar` interface alongside `ContentRegistrar`) —
    consistent with §5.3, ~30 lines;
  - or a data-driven approach if the target supports a burnable block component
    (check your 26.x targets — if a `minecraft:burnable` component exists, it is
    the cross-loader answer and costs nothing).
  - or drop it. Only `OAK_LOG_SMALL_STOOL` is registered, at 300 ticks, which is
    2.5 logs per item — a bug regardless (it makes one log yield 1.5 stools).
- **Tags.** `decor4fabric:benches`, `high_benches`, `chairs`, `small_stools`,
  `tables` are used by `Sit` and by datapacks. Keep all five. **Today**
  (`benches` = 16: the 8 `*_log_bench` **and** the 8 `*_log_bench_2` — which is
  why `Sit` reads one property off two families, §1.4 item 13;
  `high_benches` = the 8 `*_log_bench_3`; `chairs` = 32; `small_stools` = 16;
  `tables` = 16 → 88 tagged ids). **After the port**, with 11 woods:
  `benches` = 22, `high_benches` = 11, `small_stools` = 22, `chairs` = 44,
  `tables` = 22 → **121 tagged ids**, plus 44 fences/fence-gates that live in
  the vanilla `minecraft:fences` / `minecraft:fence_gates` tags instead, plus the
  untagged `workbench` = 166. The generator should emit these tag files too
  (§8.2), so the counts cannot drift apart.
- **`itemRegistry`** — delete.
- **`Sit.INCORRECT_VERSION`** — delete; superseded by the ping's message (§3.1.1).
- **`getModVersion()`** — delete. `PROTOCOL_VERSION` replaces it (§3.1.1).
- **`serverDecor` / `clientDecor` login paths** — delete; superseded by the ping.
  Both classes then likely disappear entirely rather than being ported.

---

## 10. Phase 7 — CI, and the duplication gate

### 10.1 The one idea worth stealing from compress-em

compress-em deliberately **duplicates** the `common` tree per version instead of
using a cross-version shared source set, and enforces the duplication with a
plain `diff -r` as the **first** CI step — no JDK needed, fails in ~1 second
instead of after a 10-minute build:

```bash
diff -r --exclude=pack.mcmeta --exclude=decor4fabric.accesswidener \
        --exclude=decor4fabric.classtweaker \
  versions/1.21.11/common/src/main versions/26.1/common/src/main
diff -r --exclude=pack.mcmeta \
  versions/26.1/common/src/main versions/26.2/common/src/main
diff -r --exclude=pack.mcmeta \
  versions/26.1/common/src/main versions/26.3/common/src/main
```

The `--exclude` list **is the specification**: "these are the sanctioned
divergences and nothing else may appear." Today compress-em has exactly one
divergent Java file and three divergent resources. If your port also needs
exactly one version-divergent class, this gate tells you so for free.

This matters for decor4fabric more than it did for compress-em, because you
*will* accumulate sanctioned divergences. The ones to expect, each of which
goes in the `--exclude` list by name so the gate doubles as the spec:

- the access-control file (`.accesswidener` on 1.21.11 vs `.classtweaker` on
  26.x) — already in the exclude list above;
- `WorkBenchRecipe.java`, **if** `SingleRecipeInput` is named `RecipeInput` on
  1.21.11 (§3.2.1). Verify before assuming you need this;
- possibly the four `pack.mcmeta` files, since `min_format`/`max_format` differ
  per target and cannot be identical by definition.

If a divergence appears that you cannot name a reason for, that is what the gate
is for — it fails in a second instead of after a ten-minute build.

### 10.2 The workflow

```yaml
name: build
on: [pull_request, push]
concurrency: { group: ${{ github.workflow }}-${{ github.ref }}, cancel-in-progress: true }
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: diff common trees          # FIRST, no JDK, ~1s
        run: |
          diff -r --exclude=pack.mcmeta --exclude=decor4fabric.accesswidener \
                  --exclude=decor4fabric.classtweaker \
            versions/1.21.11/common/src/main versions/26.1/common/src/main
          diff -r --exclude=pack.mcmeta versions/26.1/common/src/main versions/26.2/common/src/main
          diff -r --exclude=pack.mcmeta versions/26.1/common/src/main versions/26.3/common/src/main
      - uses: gradle/wrapper-validation-action@v2
      - uses: actions/setup-java@v4
        with: { distribution: temurin, java-version: 25 }
      - uses: gradle/actions/setup-gradle@v4
      - run: ./gradlew prismDoctor       # proves the 8 projects are wired right
      - run: ./gradlew build              # NOT `clean build` — see §4.3
      - uses: actions/upload-artifact@v4
        with:
          path: |
            versions/*/*/build/libs/*.jar
            !**/*-dev.jar
            !**/*-sources.jar
          if-no-files-found: error        # a build that produces 0 jars must FAIL
```

Two JDKs are needed only if you target 1.21.11 (Java 21) *and* 26.x (Java 25).
`foojay-resolver-convention` handles provisioning them; the Gradle daemon
itself must run on 25, so if you `setup-java` twice, explicitly point
`JAVA_HOME` back at 25 afterwards rather than relying on step order.

### 10.3 Repo hygiene

Add `.gitattributes` (LF for `gradlew`, CRLF for `*.bat`, `* text=auto`) — a
multi-JDK Windows/Linux matrix corrupts the wrapper scripts otherwise. Extend
`.gitignore` with `runs/` (Prism's dev runtime dir; the legacy Loom one is
`run/`, and both exist in your current ignore).

---

## 11. Phase 8 — Definition of done

compile-time gates cannot see half of these. compress-em's CHANGELOG records
eight port breakages, and the two worst (the `Block id not set` NPE and the
NeoForge `Registry is already frozen`) were found **only by launching the game**.
So the runtime list is not optional.

### 11.1 Static

- [ ] `./gradlew build` green for all 8 loader projects, on the Gradle 9.7.1 / JDK 25 / Prism 0.6.0 matrix.
- [ ] `./gradlew prismDoctor` reports the expected mapping mode and underlying plugin for every target.
- [ ] The `diff -r` gate is green, and the `--exclude` list matches the sanctioned divergences exactly.
- [ ] Every target has its own `pack.mcmeta` with the right `min_format`/`max_format` (re-verify against minecraft.wiki).
- [ ] `LICENSE`, `README`, `fabric.mod.json`, and all four `neoforge.mods.toml` all read **CC BY-NC-SA 4.0** (decision 3.4). The current `fabric.mod.json` still says `cc-by-sa-4.0` — that is the one file to fix.
- [ ] No `*.accesswidener` in a 26.x tree, no `*.classtweaker` in a 1.21.11 tree.
- [ ] `grep -rn "net.fabricmc.fabric.impl" versions/` returns nothing (no internal Fabric API).
- [ ] `grep -rn "Float.parseFloat\|ServerLoginNetworking\|ClientLoginNetworking" versions/` returns nothing (decision 3.1 — the old mechanism is gone, and `PROTOCOL_VERSION` replaced the float compare).
- [ ] `grep -rln "CuttingRecipe" versions/` returns nothing (decision 3.2 — the superclass is gone).
- [ ] The generated resource set equals the registered block set, by the assertion in §8.2 — **166 blocks, 166 blockstates, 166 item models, 166 loot tables, 166 recipes, 169 lang keys.**
- [ ] The generator's registry-vs-files assertion passes on all four targets.

### 11.2 Per-target runtime, on **both** loaders

- [ ] Client boots; the four creative tabs render with the right icons and every item shows a **translated** name.
- [ ] `/give` each of the **166** blocks; every one places, breaks, and drops itself.
- [ ] **`decor4fabric:workbench` drops itself** (§1.4 item 3 was broken).
- [ ] The workbench opens with a **populated recipe book** — scrollable, all
      166 recipes listed, not an empty grid (§3.2.1; this is the check that
      catches a missing `RecipeBookMenu`).
- [ ] Every `decor4fabric:workbench` recipe crafts the right output at the right count (notably `1 log → 3`, and `1 log → 1 chair`).
- [ ] Fences and fence gates connect, and their **models rotate correctly** in all four directions and all four connection states.
- [ ] Benches rotate to face the player; the `axe_type` model swaps for all six axe tiers; the axe is retrievable and **not duplicated or lost**.
- [ ] A bench with **no axe** renders facing correctly on all four sides (§8.2 — this was broken on 1.18.2 on two of four).
- [ ] Storing an axe, breaking the bench, and re-placing it **preserves the axe** (the `onRemove` migration in §5.5).
- [ ] Chairs, benches (all three heights), and stools are all sittable; dismount works; **no seat is permanently "occupied"** after dismounting (the key-mismatch bug in §1.4 item 7).
- [ ] Two players can't occupy the same seat; the seat is released when the first player walks away.
- [ ] A seat survives a chunk unload and a dimension change without leaking occupancy (§7).
- [ ] Waterlogging still works on benches, stools, and the workbench; fluid doesn't leak.
- [ ] Comparator output still reflects stored-axe state.
- [ ] **Cherry, mangrove and pale oak** each place, break, drop, craft from, connect
      as fences, and sit — one full pass per new wood (§3.3.1).
- [ ] The three new woods appear in the right creative tabs and the right tags; all 22 fences / 22 fence gates are in `minecraft:fences` / `minecraft:fence_gates`.
- [ ] Joining with a deliberately mismatched client shows the friendly version
      message from the ping, and the world was visible for only a moment first
      (§3.1.1).
- [ ] Joining with a matching client is silent — no ping, no message, no log noise.
- [ ] Every one of the **121** block ids from 1.18.2 still resolves. Open a copy of a real 1.18.2 world and place one of each family. This is the compatibility contract (§5.1) and it is non-negotiable.

### 11.3 Docs

- [ ] `README.md`: build instructions, the version-matrix table, the Java requirement per line, the Prism `clean build` warning, the license.
- [ ] `CHANGELOG.md`: Keep-a-Changelog + SemVer, `2.0.0` as a **breaking** change, with a `### Fixed` section listing every runtime break found in §11.2 and a `### Migration notes` section listing any removed ids.
- [ ] Tag `v1.2-1.18.2` on the old `main` before you replace it.

---

## 12. Trap list

Version-sensitive items, flagged with the *exact* failure so you can recognise
them. Verify each against your target's decompiled sources rather than trusting
memory.

| # | Trap | Symptom | Where |
|---|---|---|---|
| 1 | `Block id not set` | `NullPointerException` from `BlockBehaviour$Properties.effectiveDrops()` | `Properties.setId(key)` mandatory since 1.21.2 |
| 2 | `Registry is already frozen` | `IllegalStateException` at NeoForge startup | defer **construction**, not registration (§5.3) |
| 3 | `Missing license (<modfile>)` | FML refuses every NeoForge jar | `license` belongs at the **root** of `neoforge.mods.toml`, not in `[[mods]]` |
| 4 | Raw ids instead of names | every item shows `decor4fabric.oak_log_bench` | `useBlockDescriptionPrefix()` — `getDescriptionId()` is `final` |
| 5 | `displayItems` won't compile | 26.1 made `CreativeModeTab$Output` `protected`; the interface becomes unimplementable | access widener / class tweaker |
| 6 | Nothing drops | loot table directory renamed `loot_tables/` → `loot_table/` | §8.2 |
| 7 | Items drop but are wrong-tier, or don't drop at all | `Tool.isCorrectForDrops` returns **false when nothing matches at all**; you need both the block's tier and the `minecraft:tool` component | `needs_*_tool` + `mineable/axe` tags |
| 8 | `logoFile` warning | NeoForge 26.2 deprecated it | `iconFile` on 26.2+, `logoFile` below |
| 9 | NE floor locks out patch users | `${neoforge_version}` → `[26.1.2.112,)` | bare minor line `[26.1,)` (§2.4) |
| 10 | Every target fails after a clean | `NoSuchFileException: *_accesstransformer.cfg` | Prism 0.6.0 `clean build` bug (§4.3) |
| 11 | `pack.mcmeta` build failure | Prism refuses any subproject with `assets/`+`data/` and no `pack.mcmeta` | one per version per loader |
| 12 | Missing Forge refmap | MDG Legacy targets need `"refmap": "decor4fabric.refmap.json"` in every mixin json; Fabric doesn't | only matters if you add a `forge` target |
| 13 | Mod not found on Modrinth | `minecraftVersions(...)` publish list is stale and fails **silently** | §2.4 |
| 14 | Java toolchain failure | Gradle 9.7.1 itself needs JDK 25; add foojay if 1.21.11 (21) and 26.x (25) are both targeted | §10.2 |
| 15 | `Float.parseFloat("1.20.1")` | mod-version compare silently becomes 0.0f | §1.4 item 8 — `getModVersion()` deleted per §3.1.1 |
| 16 | Handler leak | `UseBlockCallback` registered on every sit | `logBench`/`logBench2`/`logBench3` `onUse` call `sitMain()` |
| 17 | Seat stuck "occupied" forever | seat rejects new sitters after any non-dismount removal | `SitEntity.remove` removes the map by exact `getPos()`; insert used integer block coords (§1.4 item 7) |
| 18 | Bench renders unrotated on south/west | same model on 2 of 4 facings, only when no axe is stored | `axe_type: 0` blockstate parts missing `"y": 180`/`"y": 270` (§8.2) |
| 19 | Oak chairs unobtainable | `crafting_shapeless` fails for the two oak chairs | the only two recipes are the *misnamed* `oak_acacia_log_chair*` files — rename, don't delete (§1.4 item 1) |
| 20 | Workbench opens **empty** | grid is there, recipe book has no entries, nothing craftable by hand | handler doesn't implement `RecipeBookMenu` — the single most-missed step of decision 3.2 (§3.2.1) |
| 21 | Recipes vanish on one target | "Unknown recipe type" in the log on 1.21.11 only | `RecipeType` not in `Registries.RECIPE_TYPE` on both loaders, or JSON shape differs per target (§3.2.1) |
| 22 | Version string compare breaks again | mismatch kick on a correct client | `PROTOCOL_VERSION` int compare, never a version-string compare (§3.1.1) |
| 23 | A new wood renders purple/black | missing model, or `Block id not set` | the 45 new blocks need `Properties.setId(key)` like the rest — `setId` is per-block, not per-family (§5.2) |

---

## 13. Suggested order of operations

```
tag v1.2-1.18.2 on main, freeze it
  │
  ├─ Phase 0  DONE 2026-09-28 — §3.1–3.4 answered and recorded
  ├─ Phase 1  Prism skeleton, 4 targets, empty       (§4)
  │     verify: 8 jars build, runClient boots both loaders on 26.1
  │
  ├─ Phase 3  Sit redesign FIRST, on a stub block   (§7)
  │     why first: it forces the blockstate shape (OCCUPIED) that
  │              the content table and the generator then have to agree with
  │
  ├─ Phase 2  Content table + registrar + block classes   (§5)
  │     11 woods from the start — the table is wood × shape, so
  │     cherry/mangrove/pale oak cost 3 entries, not 45 hand-written blocks
  │     verify: 166 blocks register, no raw-id item names
  │
  ├─ Phase 6  Fuel, tags, stragglers                       (§9)
  ├─ Phase 4  Workbench recipe + GUI rewrite                (§6, §3.2.1)
  │     recipe first (codec + RecipeBookMenu), then the screen
  ├─ Phase 5  Resource generator, run once                 (§8)
  │     verify: 166 each of blockstate/model/item/loot/recipe,
  │             169 lang keys, registry-vs-files assertion, both loaders
  │
  └─ Phase 7  CI + duplication gate + docs                 (§10, §11)
```

Phase 3 before Phase 2 is deliberate. Occupancy-as-blockstate changes the shape
of every seatable block, and the content table is where that has to be
expressed. Doing the table first means doing it twice. Note that §5 and §6 are
documented out of execution order — §6 (workbench) is numbered 4 for that
reason, not because it can be skipped ahead to.

**Build the 11 woods into the table from the first commit, not as an
afterthought.** Decision 3.3 was cheap only because the table is parameterised;
retrofitting 45 blocks onto a table written for 8 is the expensive version of
the same work, and it would also mean re-running the generator. The one thing
worth deferring if Phase 5 slips: the new woods are a clean subset to drop,
because nothing else references them (§3.3.2).

---

## 14. Where the reference material is

| Question | Source |
|---|---|
| Multi-version/multi-loader wiring | `../compress-em/settings.gradle.kts`, `build.gradle.kts` |
| The `ContentRegistrar` seam | `../compress-em/versions/26.1/common/src/main/java/net/gmsgarcia/compress/ContentRegistrar.java` + `VanillaRegistrar.java` |
| NeoForge deferred registration | `../compress-em/versions/26.1/neoforge/src/main/java/net/gmsgarcia/compress/neoforge/NeoForgeContentRegistrar.java` |
| Entry points | `.../fabric/FabricCompressEm.java`, `.../neoforge/NeoForgeCompressEm.java` |
| Content-as-data | `.../common/.../content/{BlockSpec,BlockFamilies,CompressBlocks}.java` |
| The NeoForge/MC version-scheme trap | `../compress-em/versions/26.1/neoforge/src/main/resources/META-INF/neoforge.mods.toml` (long comment) |
| Fabric/NeoForge range-syntax divergence | `../compress-em/versions/26.3/fabric/.../fabric.mod.json` vs the TOML |
| One-jar-per-minor-line policy | `../compress-em/settings.gradle.kts` (comment) |
| `clean build` bug | `../compress-em/README.md` |
| CI + duplication gate | `../compress-em/.github/workflows/build.yml` |
| 8 concrete port breakages | `../compress-em/CHANGELOG.md` `### Fixed` |
| Prism DSL | https://prism.leclowndu93150.dev/reference/dsl |
| Prism per-loader behaviour | https://prism.leclowndu93150.dev/configuration/loaders |
| Prism project layouts | https://prism.leclowndu93150.dev/configuration/project-structure |
| Prism gotchas | https://prism.leclowndu93150.dev/faq |
| MC version / pack formats | https://minecraft.wiki |
| Fabric versions | https://meta.fabricmc.net/v2/versions/game |
