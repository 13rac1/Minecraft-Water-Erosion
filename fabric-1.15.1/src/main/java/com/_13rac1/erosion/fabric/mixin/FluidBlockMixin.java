package com._13rac1.erosion.fabric.mixin;

import java.util.Random;

import net.minecraft.block.FluidBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com._13rac1.erosion.common.FluidLevel;
import com._13rac1.erosion.common.Tasks;

@Mixin(FluidBlock.class)
public class FluidBlockMixin extends Block {

	public FluidBlockMixin(Settings settings) {
		super(settings);
	}

	@Override
	public boolean hasRandomTicks(BlockState state) {
		// Water only, not Lava.
		return state.getBlock() == Blocks.WATER;
	}

	@Inject(method = "randomTick", at = @At("HEAD"), require = 1)
	private void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand, CallbackInfo info) {

		Integer level = state.get(FluidBlock.LEVEL);

		Tasks.maybeSourceBreak(state, world, pos, rand, level);

		// Skip source blocks, only flowing water.
		if (level == FluidLevel.SOURCE) {
			return;
		}

		if (Tasks.maybeFlowingWall(state, world, pos, rand, level)) {
			// Return if the flow breaks a wall.
			return;
		}

		Tasks.maybeErodeEdge(state, world, pos, rand, level);
	}

}
