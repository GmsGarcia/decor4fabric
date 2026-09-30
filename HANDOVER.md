# Handover — Decor4Fabric multi-version port

Written 2026-09-30, at commit `35db3e3` plus the work committed alongside this
file. Read this first, then `PORTING_PLAN.md` (the long-form design record —
it is 100 KB and authoritative on *why*, this file is authoritative on *now*).

---

## 1. What this repo is

A Prism-managed Minecraft mod. **The entire port is Prism 0.6.0 with no
per-version build scripts.** That is the single most important thing to
internalise before editing anything.

- `build.gradle.kts` and `settings.gradle.kts` are the only build scripts.
  There must be no `build.gradle.kts` under `versions/` — adding one conflicts
  with Prism's own configuration.
- `common/` at the root is shared across all four versions and **must not
  reference any Minecraft class.** It is documentation-only right now.
- `versions/<mc>/common/` is a per-version source tree. Code is deliberately
  **duplicated** across versions rather than shared, and CI enforces the copies
  stay identical. When you fix a bug in one, fix it in all of them.

| | 1.21.11 | 26.1 | 26.2 | 26.3 |
|---|---|---|---|---|
| Java | 21 | 25 | 25 | 25 |
| Fabric | done | done | done | stub |
| NeoForge | done | done | done | stub |

**1.21.11, 26.1 and 26.2 are ported and building. 26.3 is a skeleton only** — the
`settings.gradle.kts` entry exists but no content has been ported to it yet.
Treat 26.3 as the next major work item.

Targets are one per Minecraft *minor* line, never per patch. Do not add a
`26.1.2` target.

---

## 2. Environment

Requires **JDK 21 and 25**. The foojay toolchain resolver downloads whichever
is missing, so you do not install both by hand.

```bash
./gradlew build            # all targets, both loaders
./gradlew prismDoctor      # report toolchain + mappings per target
./gradlew :26.1:fabric:runClient
```

### Never run `clean build` as a single command

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

---

## 3. Where the work stands

### Done and verified

- Phase 5 resource generation. Nothing under `src/main/generated/` is
  hand-maintained; it is produced by `ResourceGenerator` from a block-family
  table. The three generated trees are byte-identical to each other.
- `ResourceGenerator.assertReferencesResolve` — resolves every non-vanilla
  `parent`, `model` and `textures` reference in the generated tree. This exists
  because twelve axe textures were once missing from a tree whose *generated
  files* were all individually correct, so a registry-coverage check could not
  have caught it. It is verified by a negative test: removing
  `stone_axe_rot.png` makes the generator fail with
  `generated resources reference 2 file(s) that do not exist`.
- `LogFenceGateBlock` no longer hardcodes `WoodType.OAK`. It inherits
  `FenceGateBlock.codec()`, which already carries a `wood_type` field. The old
  override meant ten of the eleven log gates round-tripped through
  serialisation with the wrong wood — the wrong sounds.
- The "Height limit for building is 319" actionbar message: **root caused**, see
  §5 below. It is vanilla's mount code, not a port defect.

### Known open items, roughly in priority order

1. **Crafting recipes are not implemented.** Blocks exist and are placeable but
   there is no recipe, so a fresh world cannot obtain them. Blocked on Phase 4
   implementing and registering `WorkBenchRecipe`. This is the biggest gap.
2. **Bench axe models are rejected at load.** The log reports the bench axe block
   models being dropped for referencing sprites that live in the *item* atlas
   (`decor4fabric:item/wooden_axe_rot` and friends). Minecraft will not let a
   block model cross atlases. Almost certainly a one-line fix in
   `ResourceGenerator`, but it has not been done. The axes will look wrong in
   game until it is.
3. **26.3 is unported.**
4. **Manual sit tests outstanding**: sneak-vs-sit, anchor return on dismount,
   second-player rejection, break/replacement cleanup, chunk unload/reload
   cleanup, axe behaviour, carpet behaviour, high bench.
5. **The negative test for table legs is missing.** An earlier attempt used an
   invalid regex and proved nothing. Needs a real one.
6. **`.github/workflows/build.yml` is stale** — pins Java 17 and references old
   paths/actions. It has not been updated for the Prism layout.

---

## 4. Git hygiene warning — read this before `git status`

`git status` reports **~900 modified files** that are not modified. Every
generated JSON carries a `* text=auto` gitattribute, and on Windows the working
tree is LF while the index is CRLF, so git's stat cache is perpetually dirty.

**The content is identical.** Always confirm with:

```bash
git diff --exit-code        # empty == nothing really changed
git diff --name-only        # the actual list
```

The real current change set is 7 modified files plus 48 new texture PNGs:

```
PORTING_PLAN.md
versions/{1.21.11,26.1,26.2}/common/.../blocks/LogFenceGateBlock.java
versions/{1.21.11,26.1,26.2}/common/.../generator/ResourceGenerator.java
versions/{1.21.11,26.1,26.2}/common/.../textures/   (16 PNGs each)
```

Add files explicitly. **Never** `git add -A` or `git add .` here — it will stage
thousands of phantom line-ending changes and produce an unreviewable diff.

---

## 5. The "Height limit for building is 319" investigation

Fully documented in `PORTING_PLAN.md` §15.6. The short version, so nobody
re-investigates it:

- The text is vanilla `build.tooHigh`, from
  `ServerPlayer.sendBuildLimitMessage(boolean, int)`. Nothing in this repo
  contains that string.
- `ServerPlayer.startRiding(...)` calls `this.connection.teleport(...)`
  **unconditionally, on every mount of anything.** That sets
  `ServerGamePacketListenerImpl.awaitingPositionFromClient`.
- For as long as that field is non-null, `handleUseItemOn` refuses block
  interactions — and the branch that catches this is the only one of the six
  call sites with **no height check at all**. It passes `level.getMaxY()` purely
  as a display argument. That is why the number read 319 overworld and 255 in
  the Nether, and why it never matched the player's actual Y. **The message
  lies about the reason.** The click really was refused, just not for a height
  problem.
- `isTooHigh` only selects which lang key is used. It checks nothing.
- The field clears when the client acks the teleport with a matching id.
  `updateAwaitingTeleport` re-sends every 20 ticks if the ack never comes.

**Still unproven, and it matters:** whether the window is transient or permanent.

- Our mount is *server*-initiated from inside a `UseItemOn` packet, so the
  client is mid-interaction when the teleport lands and may defer its ack.
  Vanilla mounts are *client*-initiated, so the client is already ready to ack.
  That is a real difference, and a vanilla-horse control test reportedly did
  **not** reproduce the message.
- Therefore the claim "it is vanilla" is **not established**. The plan currently
  records it as resolved; treat that verdict as provisional.
- **The two tests that settle it**, both cheap:
  1. Sit, wait a full 3 seconds doing nothing, *then* right-click a block. No
     message → transient, cosmetic, document and move on. Message still appears
     → the ack never arrives, the player can never interact while seated, and
     that is severe and ours to fix.
  2. Breakpoint at `ServerGamePacketListenerImpl.handleAcceptTeleportPacket`
     (~line 527 in the decompiled source). Never hits → the client is not
     acking. Hits but `packet.getId() != this.awaitingTeleport` → we are in the
     20-tick retry loop and the ids are racing.

Do not "fix" this by reflectively nulling the field — the client is
mid-handshake with it and that would desync. The guard is doing its job.

### A real bug found in the same area, unrelated to the above

`Sit.java:150-152` comments claim the third argument to `startRiding` is
`ignoreCamera` and passes `true`. The parameter was renamed to
`sendEventAndTriggers` in 1.21.x. The comment is wrong and we are actually
firing mount events and triggers. Harmless to the bug above, but fix the
comment and the intent.

---

## 6. Debugging and hot reload

Minecraft sources are **not** generated by default. Until you do, you cannot
set breakpoints in vanilla code:

```bash
./gradlew :26.1:fabric:genSourcesWithVineflower
```

Takes a few minutes, once per target. Then in IntelliJ: **File → Reload All
Gradle Projects**. If sources still do not attach, do it manually via
**Project Structure → Modules → Dependencies → Attach Sources**, pointing at the
`-sources.jar` Loom produced next to the named jar.

Line numbers in IntelliJ will **not** match any external decompilation. Always
search for the method name rather than jumping to a line.

**Where the 26.1 dev jar lives** (found under the Gradle cache, handy for
`javap`/decompiling by hand):

```
%USERPROFILE%\.gradle\caches\...\minecraft-common-deobf-26.1.2.jar
```

### Hot reload, honestly

| What | How |
|---|---|
| Method bodies | IntelliJ HotSwap. Debug, edit, **Build → Build Project**. |
| Generated resources / models | `./gradlew generateAllResources`, then **F3+T** in game. No restart. This is the fast loop for asset work. |
| New methods/fields, signature changes | **Restart.** |

Do not promise structural hot reload. 26.x needs JDK 25 and DCEVM consistently
lags new JDKs; the old `-XX:+AllowRedefinitionToAddDeleteMethods` flag was
removed in JDK 13 and there is no replacement. 1.21.11 on JDK 21 has better
odds if it is ever genuinely needed.

Singleplayer runs client and integrated server in **one JVM**, so one debug
session covers both. If a breakpoint freezes the client, right-click a thread in
the debug view and set it to not suspend *All*.

---

## 7. Conventions to keep

- **Fix in all ported versions, always.** 1.21.11, 26.1, 26.2 in one pass.
- **Comments explain *why*, not *what*.** The existing code carries
  deliberately detailed rationale for non-obvious decisions, including
  decisions that were reversed. Preserve that density; a future reader needs to
  know what was already tried and rejected, not just what the current state is.
- **Prefer fixing a generator over editing generated output.** If a JSON in
  `src/main/generated/` is wrong, the bug is in `ResourceGenerator` or in the
  block-family table. Editing the JSON directly will be silently undone.
- **Do not add mixins or reflection hacks.** Previous attempts at reflection
  diagnostics were built, used, and removed. If something needs a mixin, say so
  explicitly rather than smuggling it in.
- **Record findings in `PORTING_PLAN.md`**, including negative results and
  things that turned out not to be bugs. That file's value is largely in what
  it rules out.

---

## 8. First actions for whoever picks this up

1. `./gradlew build` and confirm it is green before changing anything.
2. Read `PORTING_PLAN.md` §8.2 (`SitEntity` and the sit architecture) and §15
   (handover) at minimum.
3. Run the two §5 tests and correct the §15.6 verdict, which is currently
   provisional.
4. Fix the bench axe atlas bug in `ResourceGenerator` — smallest real bug
   currently known, and it is user-visible.
5. Then decide whether to implement `WorkBenchRecipe` (biggest gap) or port
   26.3 (biggest new surface).
