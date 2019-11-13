package com._13rac1.erosion.mixin;

import java.util.Arrays;

import java.util.List;
import java.util.Random;

import net.minecraft.block.FluidBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FluidBlock.class)
public class FluidBlockMixin extends Block {
	// TODO: Differing erosion resistance depending on the type of block.
	// leaves < sand < gravel < farmland < dirt < grass_path < grass
	static private List<Block> ErodableBlocks = Arrays.asList(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.SAND,
			Blocks.COAL_ORE, Blocks.COBBLESTONE, Blocks.COBBLESTONE_WALL, Blocks.MOSSY_COBBLESTONE, Blocks.GRASS_PATH,
			Blocks.GRAVEL, Blocks.FARMLAND, Blocks.ACACIA_LEAVES, Blocks.BIRCH_LEAVES, Blocks.DARK_OAK_LEAVES,
			Blocks.JUNGLE_LEAVES, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES);

	// https://github.com/vktec/butterfly/blob/e8411285/src/main/java/uk/org/vktec/butterfly/mixin/FluidBlockMixin.java
	public FluidBlockMixin(Settings settings) {
		super(settings);

	}

	@Override
	public boolean hasRandomTicks(BlockState state) {
		return true;
	}

	// TODO: Use Inject or Override?
	// @Override
	@Inject(method = "onRandomTick", at = @At("HEAD"), require = 1)
	private void onRandomTick(BlockState state, World world, BlockPos pos, Random rand, CallbackInfo info) {
		// Skip Lava
		if (state.getBlock() != Blocks.WATER) {
			return;
		}

		// Skip source blocks
		Integer level = state.get(FluidBlock.LEVEL);
		if (level == 0) {
			return;
		}

		// 20 percent chance of removal.
		if (rand.nextInt(100) > 20) {
			return;
		}

		// Get the block under us.
		BlockPos underPos = new BlockPos(pos).down();
		BlockState underState = world.getBlockState(underPos);
		Block underBlock = underState.getBlock();

		// Return if the block below us is not erodable.
		// TODO: Why doesn't List.contains() work for this? One of the block definitions
		// is incorrectly null.
		if (!isErodable(underBlock)) {
			// System.out.println(underBlock.getName().asFormattedString());
			return;
		}
		// System.out.println("Can erode: " + underBlock.getName().asFormattedString());

		// Return if we are not a water edge block and not level 7. Level 7, the last
		// one, is allowed to dig down to extend the water flow.
		if (!this.isEdge(world, pos) && level != 7) {
			return;
		}

		// System.out.println("pos x:" + pos.getX() + " y:" + pos.getY() + " z:" +
		// pos.getZ() + " water level: " + level);
		System.out.println("Removing block: " + underBlock.getName().asFormattedString());
		world.setBlockState(underPos, Blocks.AIR.getDefaultState(), 3);

		// Don't delete source blocks
		if (state.get(FluidBlock.LEVEL) == 0) {
			return;
		}

		// Delete the water block
		// TODO: What is the ideal integer flag value?
		world.setBlockState(pos, Blocks.AIR.getDefaultState(), 3);

		// Delete upwards until there's no water or a water source is found. The
		// goal is to lower the water level naturally without having to reflow the
		// entire stream/creek/river.
		BlockPos posUp = pos.up();
		while (world.getBlockState(pos).getBlock() instanceof FluidBlock) {
			// System.out.println("Looking above");
			if (world.getBlockState(pos).get(FluidBlock.LEVEL) == 0) {
				break;
			}
			System.out.println("Removing block above");
			world.setBlockState(posUp, Blocks.AIR.getDefaultState(), 3);
			posUp = posUp.up();
		}

		// No call to super.randomTick() since we are deleting the block at pos.
	}

	private boolean isErodable(Block block) {
		if (block.matches(BlockTags.SAND)) {
			// TODO: The ErodableBlock check doesnt work for Sand. Seems the Blocks.SAND
			// instance is null?
			return true;
		}
		for (Block erodable : ErodableBlocks) {
			if (erodable == null) {
				// This should never happen.
				continue;
			}
			if (erodable.equals(block)) {
				return true;
			}
		}
		return false;
	}

	private boolean isEdge(World world, BlockPos pos) {
		List<BlockPos> listSidePos = Arrays.asList(pos.north(), pos.south(), pos.east(), pos.west());

		for (BlockPos sidePos : listSidePos) {
			BlockPos underPos = sidePos.down();
			BlockState underState = world.getBlockState(underPos);
			Block underBlock = underState.getBlock();

			if (!(underBlock instanceof FluidBlock)) {
				// System.out.println("Did not find side under water block: " +
				// underBlock.getClass().getName());
				continue;
			}
			System.out.println("Found side under water block");

			return true;
		}

		return false;
	}
}
