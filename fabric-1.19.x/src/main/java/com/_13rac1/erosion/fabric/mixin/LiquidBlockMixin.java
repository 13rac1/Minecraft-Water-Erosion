package com._13rac1.erosion.fabric.mixin;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LiquidBlock;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com._13rac1.erosion.common.Tasks;
import com._13rac1.erosion.minecraft.EWorld;

@Mixin(LiquidBlock.class)
public class LiquidBlockMixin extends Block {
	private Tasks tasks = new Tasks();

	public LiquidBlockMixin(LiquidBlock fluidIn, Block.Properties builder) {
		super(builder);
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		// Water only, not Lava.
		return state.getBlock() == Blocks.WATER;
	}

	@Inject(method = "randomTick", at = @At("HEAD"), require = 1)
	public void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource rand, CallbackInfo info) {
		EWorld forgeWorld = new EWorld(world);

		tasks.run(state, forgeWorld, new BlockPos(pos), rand);
	}
}
