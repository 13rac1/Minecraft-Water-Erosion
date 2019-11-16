package com._13rac1.erosion.mixin;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.block.FluidBlock;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com._13rac1.erosion.ErodableBlocks;

@Mixin(FluidBlock.class)
public class FluidBlockMixin extends Block {
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
		// Water only, not Lava.
		if (state.getBlock() != Blocks.WATER) {
			return;
		}

		// Skip source blocks, only flowing water.
		Integer level = state.get(FluidBlock.LEVEL);
		if (level == 0) {
			return;
		}

		// Get the block under us.
		BlockPos underPos = pos.down();
		BlockState underState = world.getBlockState(underPos);
		Block underBlock = underState.getBlock();

		// Return if the block below us is not erodable.
		Integer underResistance = ErodableBlocks.getErosionResistance(underBlock);
		if (underResistance == ErodableBlocks.MAX_RESISTANCE) {
			// System.out.println(underBlock.getName().asFormattedString());
			return;
		}
		// underResistance into percent chance of removal.
		if (rand.nextInt(ErodableBlocks.MAX_RESISTANCE) >= ErodableBlocks.MAX_RESISTANCE - underResistance) {
			return;
		}

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
		while (world.getBlockState(posUp).getBlock() instanceof FluidBlock) {
			if (world.getBlockState(posUp).get(FluidBlock.LEVEL) == 0) {
				break;
			}
			// System.out.println("Removing block above");
			world.setBlockState(posUp, Blocks.AIR.getDefaultState(), 3);
			posUp = posUp.up();
		}
		// TODO: Anything further to do since we are deleting ourself?
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
			// System.out.println("Found side under water block");

			return true;
		}

		return false;
	}
}
