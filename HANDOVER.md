# Handover

Point-in-time note for picking this up on another machine. `PORTING_PLAN.md` is the
source of truth; this file only points at what is in flight. It used to be a full
state snapshot and was retired in `4cd5d0c` for drifting out of sync with §15.6 — kept
short on purpose, so it should stay deletable.

## State

- Branch `port/26.x`, tracking `origin/port/26.x`.
- All six targets compile and package:
  ```
  .\gradlew.bat :1.21.11:fabric:build :1.21.11:neoforge:build :26.1:fabric:build :26.1:neoforge:build :26.2:fabric:build :26.2:neoforge:build --console=plain
  ```
- Nothing here is playtested. Treat "builds" as the only evidence.

## Needs a human

1. **Player-facing axe direction.** The axe now points at whoever stored it, from
   either long side of the log. Code is complete on all six targets but has never been
   run in game. The sign inversion was found by eye and fixed blind, so this is the
   first thing to confirm or refute.
2. **Suppression mixin** (`ServerGamePacketListenerImplMixin`, 26.1/26.2 only).
   Builds, and §15.6 documents it as shipped, but it has not been exercised since
   being wired into `fabric.mod.json` / `neoforge.mods.toml`. Its `@Redirect` pins
   `ordinal = 5`, which silently re-targets if Mojang reorders `handleUseItemOn` while
   keeping six call sites — re-run the `javap` check noted in its javadoc on every
   Minecraft update, not just when it breaks.
3. **Generated resources** were regenerated: the six baked axe models and their
   textures are deleted, since the block entity renderer draws the axe now.

## Traps

- **Six copies, no enforcement.** `LogBenchBlockEntity`, `AxeStoringSeatBlock` and the
  renderer exist once per target and nothing checks they agree. Common Java is
  byte-identical across the three versions — copy it, don't hand-edit it. A green build
  on one target proves nothing about the other five; after mirroring, confirm with
  `javap` rather than trusting the build.
- **`yawFor(d)` draws the axe along `-d`.** `ItemDisplayContext.FIXED` already bakes
  vanilla's `rotation [0, 180, 0]` into the sprite, and the two half turns compose. The
  pre-existing axe therefore pointed out the *back* of the bench all along; anything
  aiming the axe at a direction has to invert for it.
- **Renderer imports differ by version.** 1.21.11 uses
  `net.minecraft.client.renderer.state.CameraRenderState`, 26.x uses
  `...state.level.CameraRenderState`. Copying a renderer between them without the swap
  compiles fine on one target and not the other.
- **Placement stores `getOpposite()`** of where the player looked, so whoever places a
  bench ends up standing on the `FACING` side. This is why the sign error was invisible
  on the side you would naturally test from.

## Next

Store an axe from both long sides and both ends, on `log_bench` and `log_bench_2`.
When the two long sides disagree, the 180 degree flip also mirrors `offsetX` (it is
applied in the rotated frame) — expected, but confirm it looks right.