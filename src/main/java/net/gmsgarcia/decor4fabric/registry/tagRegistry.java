package net.gmsgarcia.decor4fabric.registry;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;

public class tagRegistry {
    public static class Blocks {
        public static final Tag.Identified<Block> BENCHES = createTag("benches");
        public static final Tag.Identified<Block> HIGH_BENCHES = createTag("high_benches");
        public static final Tag.Identified<Block> CHAIRS = createTag("chairs");
        public static final Tag.Identified<Block> SMALL_STOOLS = createTag("small_stools");
        public static final Tag.Identified<Block> TABLES = createTag("tables");

        public static final String MOD_ID = "decor4fabric";

        private static Tag.Identified<Block> createTag(String tagName)
        {
            return TagFactory.BLOCK.create(new Identifier(MOD_ID, tagName));
        }
    }
}
