package net.gmsgarcia.decor4fabric.sitOnStuff;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.gmsgarcia.decor4fabric.registry.blockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import static net.gmsgarcia.decor4fabric.blocks.acacia_logBench2.AXE_TYPE;

public class Sit
{
	public static final int PROTOCOL_VERSION = 18;
	public static final Identifier VERSION_CHECK = new Identifier("decor4fabric", "version_check");
	public static final Text INCORRECT_VERSION = new LiteralText(String.format("Please install Decor for 1.17.1 to play on this server."));
	public static final EntityType<SitEntity> SIT_ENTITY_TYPE = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier("decor4fabric", "sit_entity"),
			FabricEntityTypeBuilder.<SitEntity>create(SpawnGroup.MISC, SitEntity::new).dimensions(EntityDimensions.fixed(0.001F, 0.001F)).build()
	);

	public static void sitMain()
	{
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if(world.isClient)
				return ActionResult.PASS;

			if(!world.canPlayerModifyAt(player, hitResult.getBlockPos()))
				return ActionResult.PASS;

			BlockState s = world.getBlockState(hitResult.getBlockPos());
			Block b = world.getBlockState(hitResult.getBlockPos()).getBlock();
			if(
				(
				 (b.getTranslationKey() == blockRegistry.SIMPLE_OAK_LOG_CHAIR.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_BIRCH_LOG_CHAIR.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_SPRUCE_LOG_CHAIR.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_DARK_OAK_LOG_CHAIR.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_ACACIA_LOG_CHAIR.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_JUNGLE_LOG_CHAIR.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_CRIMSON_STEM_CHAIR.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_WARPED_STEM_CHAIR.getTranslationKey()) ||

				 (b.getTranslationKey() == blockRegistry.SIMPLE_OAK_LOG_CHAIR_2.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_BIRCH_LOG_CHAIR_2.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_SPRUCE_LOG_CHAIR_2.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_DARK_OAK_LOG_CHAIR_2.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_ACACIA_LOG_CHAIR_2.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_JUNGLE_LOG_CHAIR_2.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_CRIMSON_STEM_CHAIR_2.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_WARPED_STEM_CHAIR.getTranslationKey()) ||

				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_OAK_LOG_CHAIR.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_BIRCH_LOG_CHAIR.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_SPRUCE_LOG_CHAIR.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_DARK_OAK_LOG_CHAIR.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_ACACIA_LOG_CHAIR.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_JUNGLE_LOG_CHAIR.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_CRIMSON_STEM_CHAIR.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_WARPED_STEM_CHAIR.getTranslationKey()) ||

				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_OAK_LOG_CHAIR_2.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_BIRCH_LOG_CHAIR_2.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_SPRUCE_LOG_CHAIR_2.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_DARK_OAK_LOG_CHAIR_2.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_ACACIA_LOG_CHAIR_2.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_JUNGLE_LOG_CHAIR_2.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_CRIMSON_STEM_CHAIR_2.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SIMPLE_STRIPPED_WARPED_STEM_CHAIR.getTranslationKey())
				)
				&& !SitEntity.OCCUPIED.containsKey(new Vec3d(hitResult.getBlockPos().getX(), hitResult.getBlockPos().getY(), hitResult.getBlockPos().getZ())) && player.getStackInHand(hand).isEmpty()
			)
			{
				SitEntity sit = SIT_ENTITY_TYPE.create(world);
				Vec3d pos = new Vec3d(hitResult.getBlockPos().getX() + 0.5D, hitResult.getBlockPos().getY() + 0.35, hitResult.getBlockPos().getZ() + 0.5D);

				SitEntity.OCCUPIED.put(pos, player.getBlockPos());
				sit.updatePosition(pos.getX(), pos.getY(), pos.getZ());
				world.spawnEntity(sit);
				player.startRiding(sit);
				return ActionResult.SUCCESS;
			}
			else if(
				(
				 (b.getTranslationKey() == blockRegistry.OAK_LOG_BENCH_3.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.BIRCH_LOG_BENCH_3.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.SPRUCE_LOG_BENCH_3.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.DARK_OAK_LOG_BENCH_3.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.ACACIA_LOG_BENCH_3.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.JUNGLE_LOG_BENCH_3.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.CRIMSON_STEM_BENCH_3.getTranslationKey()) ||
				 (b.getTranslationKey() == blockRegistry.WARPED_STEM_BENCH_3.getTranslationKey())
				)
				&& !SitEntity.OCCUPIED.containsKey(new Vec3d(hitResult.getBlockPos().getX(), hitResult.getBlockPos().getY(), hitResult.getBlockPos().getZ())) && player.getStackInHand(hand).isEmpty()
			)
			{
				SitEntity sit = SIT_ENTITY_TYPE.create(world);
				Vec3d pos = new Vec3d(hitResult.getBlockPos().getX() + 0.5D, hitResult.getBlockPos().getY() + 0.3, hitResult.getBlockPos().getZ() + 0.5D);

				SitEntity.OCCUPIED.put(pos, player.getBlockPos());
				sit.updatePosition(pos.getX(), pos.getY(), pos.getZ());
				world.spawnEntity(sit);
				player.startRiding(sit);
				return ActionResult.SUCCESS;
			}

			else if
			(
			    (
			    	(b.getTranslationKey() == blockRegistry.OAK_LOG_BENCH.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.BIRCH_LOG_BENCH.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.SPRUCE_LOG_BENCH.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.DARK_OAK_LOG_BENCH.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.ACACIA_LOG_BENCH.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.JUNGLE_LOG_BENCH.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.CRIMSON_STEM_BENCH.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.WARPED_STEM_BENCH.getTranslationKey()) ||

					(b.getTranslationKey() == blockRegistry.OAK_LOG_BENCH_2.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.BIRCH_LOG_BENCH_2.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.SPRUCE_LOG_BENCH_2.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.DARK_OAK_LOG_BENCH_2.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.ACACIA_LOG_BENCH_2.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.JUNGLE_LOG_BENCH_2.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.CRIMSON_STEM_BENCH_2.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.WARPED_STEM_BENCH_2.getTranslationKey()) ||

					(b.getTranslationKey() == blockRegistry.OAK_LOG_BENCH_3.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.BIRCH_LOG_BENCH_3.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.SPRUCE_LOG_BENCH_3.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.DARK_OAK_LOG_BENCH_3.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.ACACIA_LOG_BENCH_3.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.JUNGLE_LOG_BENCH_3.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.CRIMSON_STEM_BENCH_3.getTranslationKey()) ||
					(b.getTranslationKey() == blockRegistry.WARPED_STEM_BENCH_3.getTranslationKey())
				)
					&& !SitEntity.OCCUPIED.containsKey(new Vec3d(hitResult.getBlockPos().getX(), hitResult.getBlockPos().getY(), hitResult.getBlockPos().getZ())) && player.getStackInHand(hand).isEmpty()
					&& world.getBlockState(hitResult.getBlockPos()).get(AXE_TYPE).equals(0)
			)
			{
				SitEntity sit = SIT_ENTITY_TYPE.create(world);
				Vec3d pos = new Vec3d(hitResult.getBlockPos().getX() + 0.5D, hitResult.getBlockPos().getY() + 0.17D, hitResult.getBlockPos().getZ() + 0.5D);

				SitEntity.OCCUPIED.put(pos, player.getBlockPos());
				sit.updatePosition(pos.getX(), pos.getY(), pos.getZ());
				world.spawnEntity(sit);
				player.startRiding(sit);
				return ActionResult.SUCCESS;
			}
			return ActionResult.PASS;
		});
	}
}
