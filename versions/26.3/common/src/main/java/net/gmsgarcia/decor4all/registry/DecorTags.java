package net.gmsgarcia.decor4all.registry;

import net.gmsgarcia.decor4all.Decor4All;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

// 1.18.2's tagRegistry.Blocks, unchanged in name and content.
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
                    Identifier.fromNamespaceAndPath(Decor4All.MOD_ID, name));
        }
    }
}
