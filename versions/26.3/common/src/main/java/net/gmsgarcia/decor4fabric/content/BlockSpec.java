package net.gmsgarcia.decor4fabric.content;

import java.util.function.Function;
import net.gmsgarcia.decor4fabric.Decor4Fabric;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

/**
 * The property set behind one Decor4Fabric block family, and the one place
 * those properties are turned into a real block.
 *
 * <p>1.18.2 spread this across eight {@code FabricBlockSettings.of(Material.X)
 * .hardness(h).resistance(r).sounds(BlockSoundGroup.Y)} chains that differed only
 * in their numbers. Collapsing them into data is the point of this record, and
 * the numbers are preserved exactly -- see {@link BlockFamilies}.
 *
 * @param mapColor            replaces {@code Material}; affects map rendering only
 * @param sound               replaces {@code BlockSoundGroup}, renamed to {@code SoundType}
 * @param destroyTime         1.18.2's {@code hardness}
 * @param explosionResistance 1.18.2's {@code resistance}
 * @param factory             which block class to instantiate
 */
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

    /**
     * Builds the modern equivalent of a 1.18.2
     * {@code FabricBlockSettings.of(Material.X).hardness(h).resistance(r)
     * .sounds(BlockSoundGroup.Y)} chain.
     *
     * <p>Argument order is easy to get backwards: {@code strength(a, b)} assigns
     * {@code a} to {@code destroyTime} and {@code b} to
     * {@code explosionResistance}, so 1.18.2's {@code hardness(h).resistance(r)}
     * becomes {@code strength(h, r)}.
     *
     * <p>{@code Material} is gone and only its map colour survives. Occlusion and
     * lava-immolation tuning is left at the vanilla defaults rather than guessed
     * at, which is what 1.18.2's {@code Material.WOOD} already resolved to.
     *
     * <p>{@code setId} is not cosmetic. Block drops became data-driven in
     * 1.21.2, so {@code Block}'s constructor resolves its loot table through the
     * registry key, and omitting this fails at launch with
     * {@code NullPointerException: Block id not set} before the mod finishes its
     * first entrypoint. The key therefore has to be threaded in from the
     * registration site, the one place that knows the id.
     */
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

    /**
     * Convenience overload for the common "one key per block" case.
     *
     * <p>1.18.2 spelled this type {@code Identifier} already; it was
     * {@code ResourceLocation} for a few years and is {@code Identifier} again in
     * 1.21.11 and 26.x. Both supported versions here use the original spelling,
     * so there is one name across the whole matrix.
     */
    public static ResourceKey<Block> key(String path) {
        return ResourceKey.create(Registries.BLOCK,
                Identifier.fromNamespaceAndPath(Decor4Fabric.MOD_ID, path));
    }
}
