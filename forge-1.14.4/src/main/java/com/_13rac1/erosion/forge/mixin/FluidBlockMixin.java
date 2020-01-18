package com._13rac1.erosion.forge.mixin;

import java.util.Random;

import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Vec3d;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com._13rac1.erosion.common.ErosionWorld;
import com._13rac1.erosion.common.FluidLevel;
import com._13rac1.erosion.common.Tasks;

@Mixin(FlowingFluidBlock.class)
public class FluidBlockMixin extends Block {

  public FluidBlockMixin(FlowingFluid fluidIn, Block.Properties builder) {
    super(builder);
  }

  class ForgeWorld implements ErosionWorld {
    private World world;

    public ForgeWorld(World world) {
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
      IFluidState fluidState = state.getFluidState();
      return fluidState.getFlow(this.world, pos);
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
  private void randomTick(BlockState state, World world, BlockPos pos, Random rand, CallbackInfo info) {
    ForgeWorld forgeWorld = new ForgeWorld(world);

    Integer level = state.get(FlowingFluidBlock.LEVEL);

    Tasks.maybeSourceBreak(state, forgeWorld, pos, rand, level);

    // Skip source blocks, only flowing water.
    if (level == FluidLevel.SOURCE) {
      return;
    }

    if (Tasks.maybeFlowingWall(state, forgeWorld, pos, rand, level)) {
      // Return if the flow breaks a wall.
      return;
    }

    Tasks.maybeErodeEdge(state, forgeWorld, pos, rand, level);
  }

}
