package net.gmsgarcia.decor4all;

import java.util.function.Supplier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier;

// The platform seam between Decor4All's content tables and whichever loader is running.
public interface ContentRegistrar {

    void block(String path, ResourceKey<Block> key, Supplier<Block> factory);

    void blockEntityType(String path, ResourceKey<BlockEntityType<?>> key, BlockEntitySupplier<?> factory,
            Supplier<Block[]> validBlocks);

    void blockItem(String path, ResourceKey<Item> key, Supplier<Item> factory);

    void tab(String path, ResourceKey<CreativeModeTab> key, Supplier<CreativeModeTab> factory);

    // Queues the carpentry table menu type, the first registry added in Phase 4.
    void menuType(String path, ResourceKey<MenuType<?>> key, Supplier<MenuType<?>> factory);

    // Queues an entity type, the fourth registry the mod writes to.
    void entityType(String path, ResourceKey<EntityType<?>> key, Supplier<EntityType<?>> factory);

    // Queues the carpentry table's recipe type, the fifth registry the mod writes to.
    void recipeType(String path, ResourceKey<RecipeType<?>> key, Supplier<RecipeType<?>> factory);

    // Queues the carpentry table recipe's serializer, the sixth registry the mod writes to.
    void recipeSerializer(String path, ResourceKey<RecipeSerializer<?>> key, Supplier<RecipeSerializer<?>> factory);
}
