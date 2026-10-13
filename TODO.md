# Remaining work

Point-at-the-next-session list, separate from `HANDOVER.md` (what is in flight
now) and `PORTING_PLAN.md` (source of truth). Branch `port/26.x`.

## 1. Automatable

- [ ] Create `CHANGELOG.md` — Keep-a-Changelog + SemVer; `2.0.0` as a
      **breaking** change; a `### Fixed` section listing every runtime break
      found in `§11.2`, and a `### Migration notes` section listing the
      namespace rename `decor4fabric:` → `decor4all:` plus any removed ids.
      (`PORTING_PLAN.md §11.3:1613`)
- [ ] Add the CI smoke-test job so a mixin `ordinal` reorder can't silently
      mis-apply. (`PORTING_PLAN.md §15.6:2062`)
- [ ] Tag `v1.2-1.18.2` on the old `main` before replacing it (only tag `1.2`
      exists). (`PORTING_PLAN.md §11.3:1614`)
- [ ] Sync the stale `concurrency` snippet copied into `PORTING_PLAN.md:1478`
      (docs only — the workflow itself is already fixed).
- [ ] Optional: prune the stale remote branches `origin/1.18`, `origin/1.18.1`,
      `origin/1.18.2`.

## 2. Human-in-game only

A green build, a jar full of recipes and a clean headless server prove none of
these. Open a client and look.

- [ ] **Coverage gap:** only **26.1 Fabric** has ever been rendered. 1.21.11
      (both loaders), 26.2 and 26.1 NeoForge are unverified visually.
- [ ] Client boots; the four creative tabs render with the right icons and every
      item shows a **translated** name. (`§11.2:1575`)
- [ ] `/give` each of the **166** blocks; every one places, breaks and drops
      itself. (`§11.2:1576`)
- [ ] **`decor4all:carpenter_table` drops itself.** (`§11.2:1577`)
- [ ] The carpentry table opens with a **populated recipe book** — scrollable,
      all 166 recipes listed, not an empty grid. (`§11.2:1578`)
- [ ] Every `decor4all:carpenter_table` recipe crafts the right output at the
      right count (notably `1 log → 3`, and `1 log → 1 chair`). (`§11.2:1581`)
- [ ] **Shift-click** out of the result slot — the last unconfirmed interaction;
      plain takeout works on 26.1 Fabric. (`HANDOVER.md:88`)
- [ ] Fences and fence gates connect, and their models rotate correctly in all
      four directions and all four connection states. (`§11.2:1582`)
- [ ] Benches (since §15.7 the `axe_type` model swap is gone; the block-entity
      renderer draws the axe): facing, stored axes **not duplicated or lost**,
      and the no-axe render. (`§11.2:1583-1584`)
- [ ] Storing an axe, breaking the bench and re-placing it **preserves the
      axe**. (`§11.2:1585`)
- [ ] Storing an axe **no longer rotates** the bench (FACING is written only at
      placement). (`§15.7:2296`)
- [ ] A **modded axe** that is tagged but not an `AxeItem`, and an **untagged
      `AxeItem`** — neither path is exercised by vanilla content. (`§15.7:2286`)
- [ ] The stored axe **survives a chunk unload/reload** (the `sendBlockUpdated`
      sync path). (`§15.7:2288`)
- [ ] The axe **pose reads as "chopped into the log"** on each target (the spin,
      lean, offsets and scale are eyeballed, not derived). (`§15.7:2291`)
- [ ] The stored axe **points at whoever put it there**, from both long sides,
      and still follows the block when stored from a log end. (`§15.7:2300`)
- [ ] Decide whether the **missing crumbling overlay** matters. (`§15.7:2305`)
- [ ] Chairs, benches (all three heights) and stools are all **sittable**;
      dismount works; **no seat is permanently "occupied"** after dismounting.
      (`§11.2:1586`)
- [ ] Two players can't occupy the same seat; the seat is released when the
      first player walks away. (`§11.2:1587`)
- [ ] A seat survives a **chunk unload and a dimension change** without leaking
      occupancy. (`§11.2:1588`)
- [ ] **Waterlogging** still works on benches, stools and the workbench; fluid
      doesn't leak. (`§11.2:1599`)
- [ ] **Comparator output** still reflects stored-axe state. (`§11.2:1600`)
- [ ] **Cherry, mangrove and pale oak** each place, break, drop, craft from,
      connect as fences, and sit — one full pass per new wood. (`§11.2:1601`)
- [ ] The three new woods appear in the right creative tabs and the right tags;
      all 22 fences / 22 fence gates are in `minecraft:fences` /
      `minecraft:fence_gates`. (`§11.2:1603`)
- [ ] Joining with a **mismatched client** shows the friendly version message
      from the ping. (`§11.2:1604`)
- [ ] Joining with a **matching client is silent** — no ping, no message, no log
      noise. (`§11.2:1607`)
- [ ] **Shift-click** out of the result slot (the last unconfirmed interaction;
      see above). (`HANDOVER.md:88`)
- [ ] Models/textures/UV pass — the six seat models side-by-side; broken UV
      seams, wrong faces, mirrored textures, per-wood break particles (`#sides`).
      (`TODO` item 5, `§11.2`, `HANDOVER.md:81-92`)
- [ ] Stack two log fences and confirm the lower one's bottom renders (post
      `cullface: "up"` fix) and break particles are still per-wood.
      (`HANDOVER.md:286`)

## 3. Done / stale

- `README.md` (build instructions, version-matrix table, Java requirement per
  line, Prism `clean build` warning, license) is done in practice — only the
  `§11.3:1612` checkbox is stale.
