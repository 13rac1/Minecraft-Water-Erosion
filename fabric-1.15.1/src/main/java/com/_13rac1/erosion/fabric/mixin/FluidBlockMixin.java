package com._13rac1.erosion.fabric.mixin;

import java.util.Random;

import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3d;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com._13rac1.erosion.common.ErosionWorld;
import com._13rac1.erosion.common.Tasks;

@Mixin(FluidBlock.class)
public class FluidBlockMixin extends Block {

	public FluidBlockMixin(Settings settings) {
		super(settings);
	}

	class FabricWorld implements ErosionWorld {
		private ServerWorld world;

		public FabricWorld(ServerWorld world) {
			this.world = world;
		}

		public BlockState getBlockState(BlockPos pos) {
			return this.world.getBlockState(pos);
		}

		public Boolean setBlockState(BlockPos pos, BlockState newState, Integer flags) {
			return this.world.setBlockState(pos, newState, flags);
		}

		public int getSeaLevel() {
			return this.world.getSeaLevel();
		}

		public Vec3d getFlowVelocity(BlockState state, BlockPos pos) {
			FluidState fluidState = state.getFluidState();
			return fluidState.getVelocity(this.world, pos);
		}

		public Boolean isFluidBlock(Block block) {
			return block instanceof FluidBlock;
		}
	}

	@Override
	public boolean hasRandomTicks(BlockState state) {
		// Water only, not Lava.
		return state.getBlock() == Blocks.WATER;
	}

	@Inject(method = "randomTick", at = @At("HEAD"), require = 1)
	private void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand, CallbackInfo info) {
		FabricWorld fabricWorld = new FabricWorld(world);

		Tasks.run(state, fabricWorld, pos, rand);
	}
}
