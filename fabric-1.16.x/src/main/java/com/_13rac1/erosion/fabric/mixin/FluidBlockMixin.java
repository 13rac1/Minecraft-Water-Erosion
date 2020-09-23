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

import com._13rac1.erosion.common.Tasks;
import com._13rac1.erosion.minecraft.EBlockPos;
import com._13rac1.erosion.minecraft.EWorld;

@Mixin(FluidBlock.class)
public class FluidBlockMixin extends Block {
	private Tasks tasks = new Tasks();

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
		EWorld fabricWorld = new EWorld(world);

		tasks.run(state, fabricWorld, new EBlockPos(pos), rand);

	}
}
