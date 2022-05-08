package net.gmsgarcia.decor4fabric.sitOnStuff;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.gmsgarcia.decor4fabric.registry.blockRegistry;
import net.gmsgarcia.decor4fabric.registry.tagRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;

import static net.gmsgarcia.decor4fabric.blocks.logBench.AXE_TYPE;

public class Sit
{
	public static final int PROTOCOL_VERSION = 18;
	public static final Identifier VERSION_CHECK = new Identifier("decor4fabric", "version_check");
	public static final Text INCORRECT_VERSION = new LiteralText(String.format("Please install Decor4Fabric for 1.18.2 to play on this server."));
	public static final EntityType<SitEntity> SIT_ENTITY_TYPE = Registry.register(
			Registry.ENTITY_TYPE,
			new Identifier("decor4fabric", "sit_entity"),
			FabricEntityTypeBuilder.<SitEntity>create(SpawnGroup.MISC, SitEntity::new).dimensions(EntityDimensions.fixed(0.001F, 0.001F)).build()
	);

	public static void sitMain()
	{
		UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			if(world.isClient)
			{
				return ActionResult.PASS;
			}

			if(!world.canPlayerModifyAt(player, hitResult.getBlockPos()))
			{
				return ActionResult.PASS;
			}

			BlockState state = world.getBlockState(hitResult.getBlockPos());
			Block block = world.getBlockState(hitResult.getBlockPos()).getBlock();

			if (!SitEntity.OCCUPIED.containsKey(new Vec3d(hitResult.getBlockPos().getX(), hitResult.getBlockPos().getY(), hitResult.getBlockPos().getZ())))
			{
				boolean sneakingAndEmpty = player.getStackInHand(hand).isEmpty() && !player.isSneaking();

				// IS CHAIR?
				if(state.isIn(tagRegistry.Blocks.CHAIRS) && sneakingAndEmpty)
				{
					SitEntity sit = SIT_ENTITY_TYPE.create(world);
					Vec3d pos = new Vec3d(hitResult.getBlockPos().getX() + 0.5D, hitResult.getBlockPos().getY() + 0.35, hitResult.getBlockPos().getZ() + 0.5D);

					SitEntity.OCCUPIED.put(pos, player.getBlockPos());
					sit.updatePosition(pos.getX(), pos.getY(), pos.getZ());
					world.spawnEntity(sit);
					player.startRiding(sit);
					return ActionResult.SUCCESS;
				}
				// IS HIGH BENCH (logBench3)?
				else if (state.isIn(tagRegistry.Blocks.HIGH_BENCHES) && sneakingAndEmpty)
				{
					SitEntity sit = SIT_ENTITY_TYPE.create(world);
					Vec3d pos = new Vec3d(hitResult.getBlockPos().getX() + 0.5D, hitResult.getBlockPos().getY() + 0.3, hitResult.getBlockPos().getZ() + 0.5D);

					SitEntity.OCCUPIED.put(pos, player.getBlockPos());
					sit.updatePosition(pos.getX(), pos.getY(), pos.getZ());
					world.spawnEntity(sit);
					player.startRiding(sit);
					return ActionResult.SUCCESS;
				}
				// IS BENCH
				else if (state.isIn(tagRegistry.Blocks.BENCHES) && sneakingAndEmpty && world.getBlockState(hitResult.getBlockPos()).get(AXE_TYPE).equals(0))
				{
					SitEntity sit = SIT_ENTITY_TYPE.create(world);
					Vec3d pos = new Vec3d(hitResult.getBlockPos().getX() + 0.5D, hitResult.getBlockPos().getY() + 0.17D, hitResult.getBlockPos().getZ() + 0.5D);

					SitEntity.OCCUPIED.put(pos, player.getBlockPos());
					sit.updatePosition(pos.getX(), pos.getY(), pos.getZ());
					world.spawnEntity(sit);
					player.startRiding(sit);
					return ActionResult.SUCCESS;
				}
				// IS SMALL STOOL
				else if (state.isIn(tagRegistry.Blocks.SMALL_STOOLS) && sneakingAndEmpty)
				{
					SitEntity sit = SIT_ENTITY_TYPE.create(world);
					Vec3d pos = new Vec3d(hitResult.getBlockPos().getX() + 0.5D, hitResult.getBlockPos().getY() + 0.35D, hitResult.getBlockPos().getZ() + 0.5D);

					SitEntity.OCCUPIED.put(pos, player.getBlockPos());
					sit.updatePosition(pos.getX(), pos.getY(), pos.getZ());
					world.spawnEntity(sit);
					player.startRiding(sit);
					return ActionResult.SUCCESS;
				}
			}

			return ActionResult.PASS;
		});
	}
}
