package net.gmsgarcia.decor4all.content;

import java.util.function.Function;
import net.gmsgarcia.decor4all.Decor4All;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

// The property set behind one Decor4All block family, and the one place those properties are
// turned into a real block.
public record BlockSpec(
        MapColor mapColor,
        SoundType sound,
        float destroyTime,
        float explosionResistance,
        Function<BlockBehaviour.Properties, Block> factory) {

    public BlockSpec {
        if (destroyTime < 0.0F || explosionResistance < 0.0F) {
            throw new IllegalArgumentException("negative block strength");
        }
    }

    // Builds the modern equivalent of a 1.18.2
    // FabricBlockSettings.of(Material.X).hardness(h).resistance(r) .sounds(BlockSoundGroup.Y)
    // chain.
    public BlockBehaviour.Properties toProperties(ResourceKey<Block> key) {
        return BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .sound(sound)
                .strength(destroyTime, explosionResistance)
                .setId(key);
    }

    public Block create(ResourceKey<Block> key) {
        return factory.apply(toProperties(key));
    }

    // Convenience overload for the common "one key per block" case.
    public static ResourceKey<Block> key(String path) {
        return ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(Decor4All.MOD_ID, path));
    }
}
