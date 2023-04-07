package com._13rac1.erosion.minecraft;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import com._13rac1.erosion.common.IWorld;

public class EWorld implements IWorld {
  private Level world;

  public EWorld(Level world) {
    this.world = world;
  }

  public BlockState getBlockState(BlockPos pos) {
    return this.world.getBlockState(pos);
  }

  public Block getBlock(BlockPos pos) {
    return this.world.getBlockState(pos).getBlock();
  }

  public Boolean setBlockAndUpdate(BlockPos pos, BlockState newState) {
    return this.world.setBlockAndUpdate(pos, newState);
  }

  public int getSeaLevel() {
    return this.world.getSeaLevel();
  }

  public Vec3 getFlowVelocity(BlockState state, BlockPos pos) {
    FluidState fluidState = state.getFluidState();
    return fluidState.getFlow(this.world, pos);
  }

  public Boolean isFluidBlock(Block block) {
    return block instanceof LiquidBlock;
  }

}
