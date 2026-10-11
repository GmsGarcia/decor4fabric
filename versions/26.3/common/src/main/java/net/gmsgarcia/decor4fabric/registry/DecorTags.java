package net.gmsgarcia.decor4fabric.registry;

import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * 1.18.2's {@code tagRegistry.Blocks}, unchanged in name and content.
 *
 * <p>Only {@link #TABLES} has a code consumer -- {@code LogTableBlock} reads it
 * for both its connection test and its leg-merging shape. The other four exist so
 * Phase 3's sit entity can find seatable blocks the same way, and so third-party
 * datapacks can extend the sets.
 *
 * <p>The membership is fixed by 1.18.2's data files and is not one tag per block
 * class: {@code benches} holds only the two low benches and {@code high_benches}
 * holds the tall one, so finding every sittable bench means querying both keys.
 */
public final class DecorTags {

    private DecorTags() {
    }

    public static final class Blocks {
        private Blocks() {
        }

        public static final TagKey<Block> BENCHES = blockTag("benches");
        public static final TagKey<Block> HIGH_BENCHES = blockTag("high_benches");
        public static final TagKey<Block> CHAIRS = blockTag("chairs");
        public static final TagKey<Block> SMALL_STOOLS = blockTag("small_stools");
        public static final TagKey<Block> TABLES = blockTag("tables");

        private static TagKey<Block> blockTag(String name) {
            return TagKey.create(Registries.BLOCK,
                    Identifier.fromNamespaceAndPath(Decor4Fabric.MOD_ID, name));
        }
    }
}
