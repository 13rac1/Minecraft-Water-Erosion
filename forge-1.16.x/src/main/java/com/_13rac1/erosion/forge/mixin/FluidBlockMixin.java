package com._13rac1.erosion.forge.mixin;

import java.util.Random;

import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com._13rac1.erosion.common.ErosionWorld;
import com._13rac1.erosion.common.Tasks;
import com._13rac1.erosion.minecraft.EBlockPos;
import com._13rac1.erosion.minecraft.EVec3d;

@Mixin(FlowingFluidBlock.class)
public class FluidBlockMixin extends Block {
  private Tasks tasks = new Tasks();

  public FluidBlockMixin(FlowingFluid fluidIn, Block.Properties builder) {
    super(builder);
  }

  class ForgeWorld implements ErosionWorld {
    private ServerWorld world;

    public ForgeWorld(ServerWorld world) {
      this.world = world;
    }

    public BlockState getBlockState(EBlockPos pos) {
      return this.world.getBlockState(pos.getPos());
    }

    public Block getBlock(EBlockPos pos) {
      return this.world.getBlockState(pos.getPos()).getBlock();
    }

    public Boolean setBlockState(EBlockPos pos, BlockState newState, Integer flags) {
      return this.world.setBlockState(pos.getPos(), newState, flags);
    }

    public int getSeaLevel() {
      return this.world.getSeaLevel();
    }

    public EVec3d getFlowVelocity(BlockState state, EBlockPos pos) {
      FluidState fluidState = state.getFluidState();
      return new EVec3d(fluidState.getFlow(this.world, pos.getPos()));
    }

    public Boolean isFluidBlock(Block block) {
      return block instanceof FlowingFluidBlock;
    }

  }

  @Override
  public boolean ticksRandomly(BlockState state) {
    // Water only, not Lava.
    return state.getBlock() == Blocks.WATER;
  }

  @Inject(method = "randomTick", at = @At("HEAD"), require = 1)
  private void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random rand, CallbackInfo info) {
    ForgeWorld forgeWorld = new ForgeWorld(world);

    tasks.run(state, forgeWorld, new EBlockPos(pos), rand);
  }
}
