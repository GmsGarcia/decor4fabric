package net.gmsgarcia.decor4fabric.registry;

import net.minecraft.block.Block;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class tagRegistry {
    public static class Blocks {
        public static final TagKey<Block> TABLES = createTag("tables");

        public static final String MOD_ID = "decor4fabric";


        private static TagKey<Block> createTag(String tagName)
        {
            return TagKey.of(Registry.BLOCK_KEY, new Identifier(MOD_ID, tagName));
        }
    }
}
