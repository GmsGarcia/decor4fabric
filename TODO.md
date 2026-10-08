# Next work sessions

Point-at-the-next-session list, separate from `HANDOVER.md` (what is in flight
now) and `PORTING_PLAN.md` (source of truth). Items are ordered by size, small
first. All except 4 are AI-doable; 4 needs a human at a screen.

## 1. [done] Carpet retrieval should land in the current slot

**Done:** `takeCarpetBack` now writes `player.setItemInHand(InteractionHand.MAIN_HAND,
carpet)` directly — the empty main hand is guaranteed by the same gates this item
describes (`useWithoutItem` runs only on the main-hand attempt, and only for an
empty stack), verified with `javap -c` on both mapped jars. Original reasoning kept
for the record:

`SmallStoolBlock.takeCarpetBack` (256-line file, `useWithoutItem`'s sneak
branch) currently hands the carpet back with `player.getInventory().add(carpet)`
and *drops* it if that fails. The take-back branch is only reachable while
sneaking with an empty main hand, so that "current slot" is always empty by the
time it runs — put the carpet straight into it instead:

- Replace the `getInventory().add(...)` / `player.drop(...)` pair with a write to
  the main-hand slot (the selected hotbar slot), i.e.
  `player.setItemInHand(InteractionHand.MAIN_HAND, carpet)`. `useWithoutItem`
  carries no `InteractionHand`, and the game mode only calls it for the main
  hand, so main hand is the slot the player is looking at.
- No drop fallback needed once the slot is provably empty; if you keep one,
  make it unreachable-only.
- Mirror across all six targets (see rule below).

## 2. [done] Sitting must work with an item in the hand (anything but a carpet)

**Done:** `trySit` dropped the empty-main-hand gate (sneaking/`mayInteract`/
`OCCUPIED` kept) and now answers `SUCCESS` on the client instead of `PASS` —
with the empty-hand rule gone, a client `PASS` would fall into the item-use path
and predict a placement the server refuses because it seats instead (the click
packet goes out unconditionally either way, javap-verified). `AxeStoringSeatBlock`
answers `TRY_WITH_EMPTY_HAND` for a non-axe while its slot is free (slot occupied
still refuses with `PASS`); `SmallStoolBlock` keeps carpet placement first (a
carpet never sits; already-carpeted + carpet stays `PASS`) and routes everything
else to the sit branch; chairs and the high bench needed no change —
`BlockBehaviour.useItemOn` already defaults to `TRY_WITH_EMPTY_HAND`. Six-target
build green, sources and `javap` disassembly identical across versions. The
in-game stick-click check below still rides along with the human pass (item 4).
Original reasoning kept for the record:

Right now a player can only sit with an empty hand. Two gates enforce it, and
both must go:

- `Sit.trySit` (line ~116) returns `PASS` when `player.getMainHandItem()` is not
  empty. That is the actual "fails to sit while holding something" bug: the
  player right-clicks a seat holding (say) a stick, gets the clicking-sound /
  `interaction.pass` feedback and never sits.
- `ServerPlayerGameMode#useItemOn` only falls through to `useWithoutItem` (where
  the sit branch lives) when the block answered `TRY_WITH_EMPTY_HAND`. So every
  seat block's `useItemOn` currently *eats* held-but-unrecognized items:
  `AxeStoringSeatBlock` returns bare `SUCCESS` for a "not an axe" stack and
  `SmallStoolBlock` returns `SUCCESS` for a non-carpet — both consume the click
  and never reach `trySit`.

Plan:

- Relax `trySit`: drop the empty-hand check (keep the sneaking check and the
  `mayInteract`/`OCCUPIED` checks).
- For **benches that keep axe storage** (bench 1, see item 3): `useItemOn` must
  answer `TRY_WITH_EMPTY_HAND` for anything that is not an axe, so the click
  reaches `useWithoutItem` → `trySit` with the item still in hand. Reserve the
  full-hand sit only while the bench slot is free; a bench holding an axe must
  still refuse with `PASS`.
- For **stools**: the carpet branch stays first (placing a carpet still wins),
  but any non-carpet stack — or an empty hand — must reach `trySit`, not `SUCCESS`.
- For **benches that lose axe storage** (item 3) and for **chairs / high bench**:
  everything that is not a seat-priority interaction goes to `trySit`.
- Keep the rule that a *carpet* in hand still places on the stool server-side
  and never sits.

Verify with a `[DBG]`-style log or in-game: hold a stick, right-click a bare
bench/stool/chair → player sits.

## 3. Bench 2 (and 3) stop storing axes; they become pure seats

- **Bench 2** (`LogBench2Block`, extends `AxeStoringSeatBlock`) currently stores
  an axe like bench 1. Remove that: no `LogBenchBlockEntity`, no axe slot, no
  axe-facing, and `useItemOn` must not accept axes. It becomes a plain seat —
  sit with anything in hand (item 2 applies) at `Sit.BENCH_HEIGHT`.
- **Bench 3** (`HighBenchBlock`) already has no axe storage — confirm, and make
  sure item 2's full-hand sit works there too (it already routes straight to
  `trySit` in `useWithoutItem`).
- Bench 1 (`LogBenchBlock`) keeps the axe for now — this item does not touch it.
- Re-check `blockstates`/models: only what must change (any model baked with an
  axe?) — the axe is drawn by the block entity renderer, so a bench without the
  block entity simply shows no axe; nothing else there should move.
- The `LogBenchBlockEntity`-driven renderer already only draws when the slot is
  non-empty; after this change bench 2 never has a slot, so the renderer's
  `valid-block` set may need to shrink. Verify no `classcastexception` on
  right-clicking bench 2.

## 4. *Human-only:* fix models, textures and UV faces

AI can point at files but cannot judge the look. Run targets in Studio-free mode
(`:26.1:fabric:runClient`) and fix by eye:

- Seats: bench/bench2/bench3/chair/chair2/stool side-by-side; broken UV seams,
  wrong faces, mirrored textures, per-wood break particles (`#sides`) still
  correct after the last Blockbench re-export.
- The models live in
  `versions/{1.21.11,26.1,26.2}/common/src/main/resources/assets/decor4fabric/models/block/models/`
  (`log_bench_model.json`, `_2`, `_3`, `log_chair_model.json`, `_2`,
  `small_stool_model.json`, `small_stool_carpet_model.json`, log table parts,
  fence parts). Textures under `textures/block/` and `textures/gui/container/`.
- Any UV change must be re-exported from the Blockbench source, not hand-edited
  in the JSON, or the next re-export reverts it.

---

## Standing rules that apply to every item

- **Six copies, no enforcement.** Each Java file exists once per target; common
  Java is byte-identical across the three versions — copy a working file, don't
  hand-edit per version. 1.21.11↔26.x differ only where the trap list and
  `CarpenterTableRecipe` say they may.
- After changes: `.\gradlew.bat generateAllResources --console=plain` only if
  assets changed, then the six-target build (exact command in `HANDOVER.md`).
- After mirroring, confirm with `javap` + `Compare-Object` on the two files
  rather than trusting the build.
- Commit + push after each finished item, not all at once.