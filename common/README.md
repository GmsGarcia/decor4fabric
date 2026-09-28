# `common/`

Declared in `settings.gradle.kts` by Prism's `sharedCommon()`. This is the only
source root that is compiled into **all four** Minecraft targets
(1.21.11, 26.1, 26.2, 26.3) unchanged.

## The one rule

**Nothing in here may reference a Minecraft class.**

These targets span two mapping channels: 1.21.11 is obfuscated and remapped by
`fabric-loom-remap`, while 26.x ships unobfuscated. A single Minecraft import in
this tree breaks three or four of the eight loader projects at once, and the
error names the wrong file — the failure surfaces in a version tree, not here.

Everything that touches Minecraft goes in `versions/<mc>/common/`, which is
compiled once per target. That is where the bulk of this mod lives.

## Current contents

Empty. Phase 1 is a skeleton only. When content arrives, the shared cases will
be the things with no Minecraft in their signature at all: the block id
contract, the recipe-shape table, and the JSON generators. The compatibility
contract — the 121 existing block ids in their existing relative order, with
the 45 cherry/mangrove/pale-oak ids appended after them — belongs here, because
it must be identical on every target and nothing in it needs a class from the
game.
